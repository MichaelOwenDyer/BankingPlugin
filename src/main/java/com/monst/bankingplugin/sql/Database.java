package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountStatus;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankConfig;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.WorldNotFoundException;
import com.monst.bankingplugin.selections.*;
import com.monst.bankingplugin.selections.Selection.SelectionType;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class Database {

	private final Set<String> notFoundWorlds = new HashSet<>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String tableBanks;
	String tableAccounts;
	String tableTransactionLog;
	String tableInterestLog;
	String tableBankProfitLog;
	String tableLogouts;
	String tableFields;

	final BankingPlugin plugin;
	HikariDataSource dataSource;

	Database(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	abstract HikariDataSource getDataSource();

	abstract String getQueryCreateTableBanks();

	abstract String getQueryCreateTableAccounts();

	abstract String getQueryCreateTableTransactionLog();

	abstract String getQueryCreateTableInterestLog();

	abstract String getQueryCreateTableProfitLog();

	abstract String getQueryCreateTableLogout();

	abstract String getQueryCreateTableFields();

	abstract String getQueryGetTable();

	private int getDatabaseVersion() throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			try (Statement s = con.createStatement()) {
				ResultSet rs = s.executeQuery("SELECT value FROM " + tableFields + " WHERE field='version'");
				if (rs.next()) {
					return rs.getInt("value");
				}
			}
		}
		return 0;
	}

	private void setDatabaseVersion(int version) throws SQLException {
		String queryUpdateVersion = "REPLACE INTO " + tableFields + " VALUES ('version', ?)";
		try (Connection con = dataSource.getConnection()) {
			try (PreparedStatement ps = con.prepareStatement(queryUpdateVersion)) {
				ps.setInt(1, version);
				ps.executeUpdate();
			}
		}
	}

	/**
	 * <p>
	 * (Re-)Connects to the the database and initializes it.
	 * </p>
	 *
	 * All tables are created if necessary and if the database structure has to be
	 * updated, that is done as well.
	 *
	 * @param callback Callback that - if succeeded - returns the amount of banks and accounts
	 *                 that were found (as {@code int[]})
	 */
	public void connect(final Callback<int[]> callback) {
		if (!Config.databaseTablePrefix.matches("^([a-zA-Z0-9\\-_]+)?$")) {
			// Only letters, numbers dashes and underscores are allowed
			plugin.getLogger().severe("Database table prefix contains illegal letters, using 'bankingplugin_' prefix.");
			Config.databaseTablePrefix = "bankingplugin_";
		}

		this.tableBanks = Config.databaseTablePrefix + "banks";
		this.tableAccounts = Config.databaseTablePrefix + "accounts";
		this.tableTransactionLog = Config.databaseTablePrefix + "transaction_log";
		this.tableInterestLog = Config.databaseTablePrefix + "interest_log";
		this.tableBankProfitLog = Config.databaseTablePrefix + "profit_log";
		this.tableLogouts = Config.databaseTablePrefix + "player_logouts";
		this.tableFields = Config.databaseTablePrefix + "fields";

		Utils.bukkitRunnable(() -> {
			disconnect();

			try {
				dataSource = getDataSource();
			} catch (Exception e) {
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			if (dataSource == null) {
				Exception e = new IllegalStateException("Data source is null");
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			try (Connection con = dataSource.getConnection()) {
				// Update database structure if necessary
				if (update()) {
					plugin.getLogger().info("Updating database finished.");
				}

				plugin.debug("Starting table creation");

				for (String query : new String[] {
						getQueryCreateTableBanks(), // Create banks table
						getQueryCreateTableAccounts(), // Create accounts table
						getQueryCreateTableTransactionLog(), // Create transaction log table
						getQueryCreateTableInterestLog(), // Create interest log table
						getQueryCreateTableProfitLog(), // Create profit log table
						getQueryCreateTableLogout(), // Create logout table
						getQueryCreateTableFields() // Create fields table
				})
					try (Statement s = con.createStatement()) {
						s.executeUpdate(query);
					}

				// Clean up economy log
				cleanUpLogs(false);

				// Count accounts entries in database
				try (Statement s = con.createStatement()) {
					int accounts;
					ResultSet rsAccounts = s.executeQuery("SELECT COUNT(id) FROM " + tableAccounts);
					if (rsAccounts.next()) {
						accounts = rsAccounts.getInt(1);
						plugin.debug("Initialized database with " + accounts + " account entries");
					} else {
						throw new SQLException("Count result set has no account entries");
					}

					int banks;
					ResultSet rsBanks = s.executeQuery("SELECT COUNT(id) FROM " + tableBanks);
					if (rsBanks.next()) {
						banks = rsBanks.getInt(1);
						plugin.debug("Initialized database with " + banks + " bank entries");
					} else {
						throw new SQLException("Count result set has no bank entries");
					}

					if (callback != null) {
						callback.callSyncResult(new int[] {banks, accounts});
					}
				}
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to initialize or connect to database.");
				plugin.debug("Failed to initialize or connect to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	private boolean update() throws SQLException {

		try (Connection con = dataSource.getConnection()) {

			try (PreparedStatement ps = con.prepareStatement(getQueryGetTable())) {
				ps.setString(1, tableBanks);
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					return false;
				}
			}

			boolean needsReordering = false;
			String checkOrderQuery = "SELECT account_config FROM " + tableBanks;
			try (Statement s = con.createStatement()) {
				ResultSet rs = s.executeQuery(checkOrderQuery);
				if (rs.next()) {
					String[] accConfig = rs.getString("account_config").split(" \\| ");
					try {
						Double.parseDouble(accConfig[0]);
						needsReordering = true;
					} catch (NumberFormatException ignored) {} // Already updated
				}
			}

			if (needsReordering) {

				List<String> backupTableQueries = new ArrayList<>();
				List<String> recreateTableQueries = new ArrayList<>();

				backupTableQueries.add("ALTER TABLE " + tableBanks + " RENAME TO backup_banks"); // for backup
				recreateTableQueries.add(getQueryCreateTableBanks());

				for (String query : backupTableQueries)
					try (Statement s = con.createStatement()) {
						s.executeUpdate(query);
					}

				for (String query : recreateTableQueries)
					try (Statement s = con.createStatement()) {
						s.executeUpdate(query);
					}

				plugin.getLogger().info("Updating database... (reordering)");

				// Convert banks table
				try (Statement s = con.createStatement()) {
					ResultSet rs = s.executeQuery("SELECT id,account_config FROM backup_banks");
					while (rs.next()) {
						String[] accConfig = rs.getString("account_config").split(" \\| ");
						int offset = accConfig.length == 15 ? 1 : 0;
						List<String> newAccConfig = new ArrayList<>();
						newAccConfig.add(accConfig[3 + offset]);
						newAccConfig.add(accConfig[9 + offset]);
						newAccConfig.add(accConfig[12 + offset]);
						newAccConfig.add(accConfig[0]);
						newAccConfig.add(accConfig[8 + offset]);
						newAccConfig.add(accConfig[10 + offset]);
						newAccConfig.add(accConfig[11 + offset]);
						newAccConfig.add(accConfig[2 + offset]);
						newAccConfig.add(accConfig[4 + offset]);
						newAccConfig.add(accConfig[5 + offset]);
						newAccConfig.add(accConfig[6 + offset]);
						newAccConfig.add(accConfig[7 + offset]);
						newAccConfig.add(accConfig[13 + offset]);
						newAccConfig.add(accConfig[1]);
						newAccConfig.add(offset == 1 ? accConfig[2] : Config.interestPayoutTimes.getDefault().toString());

						String insertQuery = "INSERT INTO " + tableBanks
								+ " SELECT id,name,owner,co_owners,selection_type,world,minY,maxY,points,? FROM backup_banks"
								+ " WHERE id = ?";
						try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
							ps.setString(1, String.join(" | ", newAccConfig));
							ps.setInt(2, rs.getInt("id"));
							ps.executeUpdate();
						}
					}
				}
			}

			return needsReordering;
		}
	}

	/**
	 * Add an account to the database
	 *
	 * @param account  Account to add
	 * @param callback Callback that - if succeeded - returns the ID the account was
	 *                 given (as {@code int})
	 */
	public void addAccount(final Account account, final Callback<Integer> callback) {
		final String queryNoId = "REPLACE INTO " + tableAccounts
				+ " (bank_id,nickname,owner,co_owners,size,balance,prev_balance,multiplier_stage,remaining_until_payout,remaining_offline_payouts,remaining_offline_until_reset,world,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		final String queryWithId = "REPLACE INTO " + tableAccounts
				+ " (id,bank_id,nickname,owner,co_owners,size,balance,prev_balance,multiplier_stage,remaining_until_payout,remaining_offline_payouts,remaining_offline_until_reset,world,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			String query = account.hasID() ? queryWithId : queryNoId;

			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				int i = 0;
				if (account.hasID()) {
					ps.setInt(1, account.getID());
					i++;
				}

				ps.setInt(i + 1, account.getBank().getID());
				ps.setString(i + 2, account.getRawName());
				ps.setString(i + 3, account.getOwner().getUniqueId().toString());
				ps.setString(i + 4, account.getCoowners().isEmpty() ? null
						: account.getCoowners().stream()
								.map(p -> "" + p.getUniqueId())
								.collect(Collectors.joining(" | "))
				);
				ps.setInt(i + 5, account.getSize());

				ps.setString(i + 6, account.getBalance().toString());
				ps.setString(i + 7, account.getPrevBalance().toString());

				ps.setInt(i + 8, account.getStatus().getMultiplierStage());
				ps.setInt(i + 9, account.getStatus().getDelayUntilNextPayout());
				ps.setInt(i + 10, account.getStatus().getRemainingOfflinePayouts());
				ps.setInt(i + 11, account.getStatus().getRemainingOfflinePayoutsUntilReset());

				ps.setString(i + 12, account.getLocation().getWorld() != null ?
						account.getLocation().getWorld().getName() :
						"world");
				ps.setInt(i + 13, account.getLocation().getBlockX());
				ps.setInt(i + 14, account.getLocation().getBlockY());
				ps.setInt(i + 15, account.getLocation().getBlockZ());

				ps.executeUpdate();

				if (!account.hasID()) {
					int accountId = -1;
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						accountId = rs.getInt(1);
					}
					account.setID(accountId);
				}

				account.getBank().addAccount(account);
				account.getBank().notifyObservers();

				if (callback != null) {
					callback.callSyncResult(account.getID());
				}

				plugin.debug("Added account to database (#" + account.getID() + ").");
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to add account to database (#" + account.getID() + ").");
				plugin.debug("Failed to add account to database (#" + account.getID() + ").");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Remove an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(final Account account, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con
							.prepareStatement("DELETE FROM " + tableAccounts + " WHERE id = ?")) {
				ps.setInt(1, account.getID());
				ps.executeUpdate();

				plugin.debug("Removing account from database (#" + account.getID() + ")");

				if (callback != null) {
					callback.callSyncResult(null);
				}
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to remove account from database (#" + account.getID() + ").");
				plugin.debug("Failed to remove account from database (#" + account.getID() + ").");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Add a bank to the database
	 *
	 * @param bank     Bank to add
	 * @param callback Callback that - if succeeded - returns the ID the bank was
	 *                 given (as {@code int})
	 */
	public void addBank(final Bank bank, final Callback<Integer> callback) {
		final String queryWithId = "REPLACE INTO " + tableBanks
				+ " (id,name,owner,co_owners,selection_type,world,minY,maxY,points,account_config) VALUES(?,?,?,?,?,?,?,?,?,?)";
		final String queryNoId = "REPLACE INTO " + tableBanks
				+ " (name,owner,co_owners,selection_type,world,minY,maxY,points,account_config) VALUES(?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			String query = bank.hasID() ? queryWithId : queryNoId;

			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				int i = 0;
				if (bank.hasID()) {
					i = 1;
					ps.setInt(1, bank.getID());
				}

				ps.setString(i + 1, bank.getRawName() != null ? bank.getRawName() : "Bank");

				if (bank.isAdminBank()) {
					ps.setString(i + 2, "$ADMIN$");
					ps.setString(i + 3, null);
				} else {
					ps.setString(i + 2, bank.getOwner().getUniqueId().toString());
					ps.setString(i + 3, bank.getCoowners().isEmpty() ? null :
							bank.getCoowners().stream()
									.map(p -> "" + p.getUniqueId())
									.collect(Collectors.joining(" | "))
					);
				}
				ps.setString(i + 4, bank.getSelection().getType().toString());
				ps.setString(i + 5, bank.getSelection().getWorld().getName());

				if (bank.getSelection().getType() == SelectionType.POLYGONAL) {
					PolygonalSelection sel = (PolygonalSelection) bank.getSelection();

					ps.setInt(i + 6, sel.getMinimumPoint().getBlockY());
					ps.setInt(i + 7, sel.getMaximumPoint().getBlockY());

					String vertices = sel.getVertices().stream()
							.map(vector -> "" + vector.getBlockX() + "," + vector.getBlockZ())
							.collect(Collectors.joining(" | "));

					ps.setString(i + 8, vertices);

				} else if (bank.getSelection().getType() == SelectionType.CUBOID) {
					CuboidSelection sel = (CuboidSelection) bank.getSelection();

					StringBuilder sb = new StringBuilder(64);
					BlockVector3D max = sel.getMaximumPoint();
					BlockVector3D min = sel.getMinimumPoint();

					sb.append(max.getBlockX()).append(",").append(max.getBlockY()).append(",").append(max.getBlockZ());
					sb.append(" | ");
					sb.append(min.getBlockX()).append(",").append(min.getBlockY()).append(",").append(min.getBlockZ());

					ps.setInt(i + 6, -1);
					ps.setInt(i + 7, -1);

					ps.setString(i + 8, sb.toString());
				} else {
					IllegalStateException e = new IllegalStateException("Bank selection neither cuboid nor polygonal! (#" + bank.getID() + ")");
					plugin.debug(e);
					throw e;
				}

				ps.setString(i + 9, BankField.stream()
						.map(field -> "" + bank.get(field, true))
						.collect(Collectors.joining(" | "))
				);

				ps.executeUpdate();

				if (!bank.hasID()) {
					int bankId = -1;
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						bankId = rs.getInt(1);
					}
					bank.setID(bankId);
				}

				if (callback != null) {
					callback.callSyncResult(bank.getID());
				}

				plugin.debug("Adding bank to database (#" + bank.getID() + ")");
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to add bank to database.");
				plugin.debug("Failed to add bank to database (#" + bank.getID() + ")");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Remove a bank from the database
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(final Bank bank, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con
							.prepareStatement("DELETE FROM " + tableBanks + " WHERE id = ?")) {
				ps.setInt(1, bank.getID());
				ps.executeUpdate();

				plugin.debug("Removing account from database (#" + bank.getID() + ")");

				if (callback != null) {
					callback.callSyncResult(null);
				}
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to remove bank from database (#" + bank.getID() + ").");
				plugin.debug("Failed to remove bank from database (#" + bank.getID() + ").");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Get all banks and accounts from the database
	 *
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all banks (as
	 *                            {@code Collection<Account>})
	 */
	public void getBanksAndAccounts(final boolean showConsoleMessages, final Callback<Map<Bank, Collection<Account>>> callback) {
		Utils.bukkitRunnable(() -> {

			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableBanks + "")) {
				// id,name,selection_type,world,minY,maxY,points,account_config
				ResultSet rs = ps.executeQuery();

				Map<Bank, Set<Account>> banksAndAccounts = new ConcurrentHashMap<>();
				while (rs.next()) {

					int bankId = rs.getInt("id");

					plugin.debug("Getting bank from database... (#" + bankId + ")");

					String worldName = rs.getString("world");
					World world = Bukkit.getWorld(worldName);

					if (world == null) {
						WorldNotFoundException e = new WorldNotFoundException(worldName);
						if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
							plugin.getLogger().warning(e.getMessage());
							notFoundWorlds.add(worldName);
						}
						plugin.debug("Failed to get bank (#" + bankId + ")");
						plugin.debug(e);
						continue;
					}

					String name = rs.getString("name");
					boolean isAdminBank = rs.getString("owner").equals("$ADMIN$");
					Set<OfflinePlayer> coowners = rs.getString("co_owners") == null ?
							new HashSet<>() :
							Arrays.stream(rs.getString("co_owners").split(" \\| "))
									.filter(string -> !string.isEmpty())
									.map(UUID::fromString)
									.map(Bukkit::getOfflinePlayer)
									.collect(Collectors.toCollection(HashSet::new));
					OfflinePlayer owner = null;
					if (!isAdminBank) {
						owner = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("owner")));
					}

					String[] pointArray = rs.getString("points").split(" \\| ");
					Selection selection;
					if (rs.getString("selection_type").equals("POLYGONAL")) {
						int minY = rs.getInt("minY");
						int maxY = rs.getInt("maxY");

						List<BlockVector2D> nativePoints = new ArrayList<>();

						for (String point : pointArray) {
							String[] coords = point.split(",");
							int x = Integer.parseInt(coords[0]);
							int z = Integer.parseInt(coords[1]);
							nativePoints.add(new BlockVector2D(x, z));
						}

						selection = PolygonalSelection.of(world, nativePoints, minY, maxY);
					} else {

						String[] coords = pointArray[0].split(",");
						int minX = Integer.parseInt(coords[0]);
						int minY = Integer.parseInt(coords[1]);
						int minZ = Integer.parseInt(coords[2]);

						coords = pointArray[1].split(",");
						int maxX = Integer.parseInt(coords[0]);
						int maxY = Integer.parseInt(coords[1]);
						int maxZ = Integer.parseInt(coords[2]);

						BlockVector3D min = new BlockVector3D(minX, minY, minZ);
						BlockVector3D max = new BlockVector3D(maxX, maxY, maxZ);

						selection = CuboidSelection.of(world, min, max);
					}

					String[] accConfig = rs.getString("account_config").split(" \\| ");
					List<Integer> multipliers;
					try {
						multipliers = Arrays.stream(accConfig[13].substring(1, accConfig[13].length() - 1).split(","))
								.map(String::trim)
								.map(Integer::parseInt)
								.collect(Collectors.toList());
					} catch (NumberFormatException e) {
						multipliers = Config.multipliers.getDefault();
					}
					List<LocalTime> interestPayoutTimes;
					try {
						interestPayoutTimes =
								Arrays.stream(accConfig[14].substring(1, accConfig[14].length() - 1).split(","))
								.map(LocalTime::parse)
								.collect(Collectors.toList());
					} catch (DateTimeParseException e) {
						interestPayoutTimes = Config.interestPayoutTimes.getDefault();
					}

					BankConfig bankConfig = new BankConfig(
							Boolean.parseBoolean(accConfig[0]),
							Boolean.parseBoolean(accConfig[1]),
							Boolean.parseBoolean(accConfig[2]),
							Double.parseDouble(accConfig[3]),
							Double.parseDouble(accConfig[4]),
							Double.parseDouble(accConfig[5]),
							Double.parseDouble(accConfig[6]),
							Integer.parseInt(accConfig[7]),
							Integer.parseInt(accConfig[8]),
							Integer.parseInt(accConfig[9]),
							Integer.parseInt(accConfig[10]),
							Integer.parseInt(accConfig[11]),
							Integer.parseInt(accConfig[12]),
							multipliers,
							interestPayoutTimes
					);

					plugin.debug("Initializing bank"
							+ (name != null ? " \"" + ChatColor.stripColor(name) + "\"" : "") + "... (#" + bankId + ")");

					Bank bank = isAdminBank ?
							Bank.recreate(bankId, name, coowners, selection, bankConfig) :
							Bank.recreate(bankId, name, owner, coowners, selection, bankConfig);

					getAccountsAtBank(bank, showConsoleMessages, Callback.of(plugin,
							result -> banksAndAccounts.put(bank, result),
							callback::callSyncError
					));
				}

				if (callback != null) {
					callback.callSyncResult(Collections.unmodifiableMap(banksAndAccounts));
				}
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to get banks from database.");
				plugin.debug("Failed to get banks from database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Get all accounts from the database under a certain bank
	 *
	 * @param bank                The bank to get the accounts of
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all accounts (as
	 *                            {@code Collection<Account>})
	 */
	private void getAccountsAtBank(Bank bank, final boolean showConsoleMessages, final Callback<Set<Account>> callback) {
		try (Connection con = dataSource.getConnection();
			 PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableAccounts + " WHERE bank_id = ?")) {
			ps.setInt(1, bank.getID());
			ResultSet rs = ps.executeQuery();

			Set<Account> accounts = new HashSet<>();
			while (rs.next()) {

				int accountId = rs.getInt("id");

				plugin.debug("Getting account from database... (#" + accountId + ")");

				String worldName = rs.getString("world");
				World world = Bukkit.getWorld(worldName);

				if (world == null) {
					WorldNotFoundException e = new WorldNotFoundException(worldName);
					if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
						plugin.getLogger().warning(e.getMessage());
						notFoundWorlds.add(worldName);
					}
					plugin.debug("Failed to get account (#" + accountId + ")");
					plugin.debug(e);
					continue;
				}

				AccountStatus status;
				try {
					int multiplierStage = rs.getInt("multiplier_stage");
					int remainingUntilPayout = rs.getInt("remaining_until_payout");
					int remainingOfflinePayouts = rs.getInt("remaining_offline_payouts");
					int remainingOfflineUntilReset = rs.getInt("remaining_offline_until_reset");

					status = new AccountStatus(bank, multiplierStage, remainingUntilPayout,
							remainingOfflinePayouts, remainingOfflineUntilReset);

				} catch (SQLException e) {
					plugin.getLogger().severe("Failed to create account status (#" + accountId + ").");
					plugin.debug("Failed to create account status.");
					plugin.debug(e);
					continue;
				}

				BigDecimal balance = BigDecimal.valueOf(Double.parseDouble(rs.getString("balance")));
				BigDecimal prevBalance = BigDecimal.valueOf(Double.parseDouble(rs.getString("prev_balance")));

				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
				Location location = new Location(world, x, y, z);
				OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("owner")));
				Set<OfflinePlayer> coowners = rs.getString("co_owners") == null
						? new HashSet<>()
						: Arrays.stream(rs.getString("co_owners").split(" \\| "))
						.filter(string -> !string.isEmpty())
						.map(UUID::fromString)
						.map(Bukkit::getOfflinePlayer)
						.collect(Collectors.toCollection(HashSet::new));
				String nickname = rs.getString("nickname");

				plugin.debugf("Initializing account #%d at bank \"%s\"", accountId, bank.getName());

				Account account = Account.reopen(accountId, owner, coowners, bank, location, status, nickname, balance, prevBalance);
				accounts.add(account);
			}

			if (callback != null) {
				callback.callSyncResult(Collections.unmodifiableSet(accounts));
			}
		} catch (SQLException e) {
			if (callback != null) {
				callback.callSyncError(e);
			}

			plugin.getLogger().severe("Failed to get accounts from database.");
			plugin.debug("Failed to get accounts from database.");
			plugin.debug(e);
		}
	}

	/**
	 * Log an economy transaction to the database
	 *
	 * @param executor Player who performed a transaction
	 * @param account  The {@link Account} the player performed the transaction on
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountTransaction(final Player executor, Account account, BigDecimal amount, Callback<Void> callback) {
		if (!Config.enableTransactionLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}

		final String query = "INSERT INTO " + tableTransactionLog
				+ " (account_id,bank_id,timestamp,time,owner_name,owner_uuid,executor_name,executor_uuid,transaction_type,amount,new_balance,world,x,y,z)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement(query)) {

				long millis = System.currentTimeMillis();

				ps.setInt(1, account.getID());
				ps.setInt(2, account.getBank().getID());
				ps.setString(3, dateFormat.format(millis));
				ps.setLong(4, millis);
				ps.setString(5, account.getOwner().getName());
				ps.setString(6, account.getOwner().getUniqueId().toString());
				if (!account.isOwner(executor)) {
					ps.setString(7, executor.getName());
					ps.setString(8, executor.getUniqueId().toString());
				} else {
					ps.setString(7, null);
					ps.setString(8, null);
				}
				ps.setString(9, amount.signum() > 0 ? "DEPOSIT" : "WITHDRAWAL");
				ps.setString(10, amount.abs().toString());
				ps.setString(11, account.getBalance().toString());
				ps.setString(12, account.getLocation().getWorld() != null
						? account.getLocation().getWorld().getName()
						: "world");
				ps.setInt(13, account.getLocation().getBlockX());
				ps.setInt(14, account.getLocation().getBlockY());
				ps.setInt(15, account.getLocation().getBlockZ());
				ps.executeUpdate();

				if (callback != null) {
					callback.callSyncResult(null);
				}

				plugin.debug("Logged transaction to database");
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to log banking transaction to database.");
				plugin.debug("Failed to log banking transaction to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Log an interest payout to the database
	 *
	 * @param account  The {@link Account} the interest was derived from
	 * @param baseAmount   The {@link BigDecimal} base transaction amount
	 * @param multiplier	The multiplier of the transaction
	 * @param amount   The {@link BigDecimal} final transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountInterest(Account account, BigDecimal baseAmount, int multiplier, BigDecimal amount, Callback<Void> callback) {
		if (!Config.enableInterestLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}

		final String query = "INSERT INTO " + tableInterestLog
				+ " (account_id,bank_id,owner_name,owner_uuid,base_amount,multiplier,amount,timestamp,time)"
				+ " VALUES(?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement(query)) {

				long millis = System.currentTimeMillis();

				ps.setInt(1, account.getID());
				ps.setInt(2, account.getBank().getID());
				ps.setString(3, account.getOwner().getName());
				ps.setString(4, account.getOwner().getUniqueId().toString());
				ps.setString(5, baseAmount.toString());
				ps.setInt(6, multiplier);
				ps.setString(7, amount.toString());
				ps.setString(8, dateFormat.format(millis));
				ps.setLong(9, millis);

				ps.executeUpdate();

				if (callback != null) {
					callback.callSyncResult(null);
				}

				plugin.debug("Logged interest to database");
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to log interest to database.");
				plugin.debug("Failed to log interest to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	public void logBankCashFlow(Bank bank, BigDecimal amount, Callback<Void> callback) {
		if (!Config.enableProfitLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}

		final String query = "INSERT INTO " + tableBankProfitLog
				+ " (bank_id,owner_name,owner_uuid,amount,timestamp,time)"
				+ " VALUES(?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query)) {

				long millis = System.currentTimeMillis();

				ps.setInt(1, bank.getID());
				ps.setString(2, bank.getOwner().getName());
				ps.setString(3, bank.getOwner().getUniqueId().toString());
				ps.setString(4, amount.toString());
				ps.setString(5, dateFormat.format(millis));
				ps.setLong(6, millis);

				ps.executeUpdate();

				if (callback != null)
					callback.callSyncResult(null);

				plugin.debug("Logged profit to database");
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);

				plugin.getLogger().severe("Failed to log profit to database.");
				plugin.debug("Failed to log profit to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Cleans up the economy log to reduce file size
	 *
	 * @param async Whether the call should be executed asynchronously
	 */
	public void cleanUpLogs(boolean async) {
		if (Config.cleanupLogDays <= 0)
			return;
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			long time = System.currentTimeMillis() - Config.cleanupLogDays * 86400000L;
			String queryCleanUpTransactionLog = "DELETE FROM " + tableTransactionLog + " WHERE time < " + time;
			String queryCleanUpInterestLog = "DELETE FROM " + tableInterestLog + " WHERE time < " + time;
			String queryCleanUpLogouts = "DELETE FROM " + tableLogouts + " WHERE time < " + time;

			try (Connection con = dataSource.getConnection();
					Statement s = con.createStatement();
					Statement s2 = con.createStatement();
					Statement s3 = con.createStatement()) {
				s.executeUpdate(queryCleanUpTransactionLog);
				s2.executeUpdate(queryCleanUpInterestLog);
				s3.executeUpdate(queryCleanUpLogouts);

				plugin.getLogger().info("Cleaned up banking logs.");
				plugin.debug("Cleaned up banking logs.");
			} catch (SQLException e) {
				plugin.getLogger().severe("Failed to clean up banking logs.");
				plugin.debug("Failed to clean up banking logs.");
				plugin.debug(e);
			}
		});
		if (async)
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.run();
	}

    /**
     * Log a logout to the database
     *
     * @param player    Player who logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(final Player player, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableLogouts + " (player,time) VALUES(?,?)")) {

				ps.setString(1, player.getUniqueId().toString());
				ps.setLong(2, System.currentTimeMillis());
				ps.executeUpdate();

				if (callback != null)
					callback.callSyncResult(null);

				if (!player.isOnline())
					plugin.debug("Logged logout to database");

			} catch (final SQLException ex) {
				if (callback != null)
					callback.callSyncError(ex);

				plugin.getLogger().severe("Failed to log last logout to database.");
				plugin.debug("Failed to log logout to database.");
				plugin.debug(ex);
			}
        }).runTaskAsynchronously(plugin);
    }

    /**
     * Gets the revenue a player received in account interest while they were offline
     *
     * @param player     Player whose revenue to get
     * @param logoutTime Time in milliseconds when he logged out the last time
     * @param callback   Callback that - if succeeded - returns the revenue the
     *                   player made while offline (as {@code double})
     */
	public void getOfflineAccountRevenue(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getOfflineRevenue(player, logoutTime, tableInterestLog, callback);
    }

	/**
	 * Gets the revenue a player earned in bank profit while they were offline
	 *
	 * @param player     Player whose revenue to get
	 * @param logoutTime Time in milliseconds when he logged out the last time
	 * @param callback   Callback that - if succeeded - returns the revenue the
	 *                   player made while offline (as {@code double})
	 */
	public void getOfflineBankRevenue(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getOfflineRevenue(player, logoutTime, tableBankProfitLog, callback);
	}

    private void getOfflineRevenue(Player player, long logoutTime, String table, Callback<BigDecimal> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE owner_uuid = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();

				BigDecimal revenue = BigDecimal.ZERO;
				while (rs.next()) {
					if (rs.getLong("time") > logoutTime) {
						BigDecimal interest = BigDecimal.valueOf(Double.parseDouble(rs.getString("amount")));
						revenue = revenue.add(interest);
					}
				}
				if (callback != null) {
					callback.callSyncResult(revenue.setScale(2, RoundingMode.HALF_EVEN));
				}
			} catch (SQLException ex) {
				if (callback != null) {
					callback.callSyncError(ex);
				}

				plugin.getLogger().severe("Failed to get revenue from database.");
				plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\".");
				plugin.debug(ex);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Gets the last logout of a player
	 *
	 * @param player   Player who logged out
	 * @param callback Callback that - if succeeded - returns the time in
	 *                 milliseconds the player logged out (as {@code long}) or
	 *                 {@code -1} if the player has not logged out yet.
	 */
	public void getLastLogout(Player player, Callback<Long> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con
							.prepareStatement("SELECT * FROM " + tableLogouts + " WHERE player = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					if (callback != null) {
						callback.callSyncResult(rs.getLong("time"));
					}
				}

				if (callback != null) {
					callback.callSyncResult(-1L);
				}
			} catch (SQLException e) {
				if (callback != null) {
					callback.callSyncError(e);
				}

				plugin.getLogger().severe("Failed to get last logout from database.");
				plugin.debug("Failed to get last logout from player \"" + player.getName() + "\".");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Closes the data source
	 */
	public void disconnect() {
		if (dataSource == null)
			return;
		dataSource.close();
		dataSource = null;
	}

}
