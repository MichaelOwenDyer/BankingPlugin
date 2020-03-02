package com.monst.bankingplugin.sql;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountTransactionEvent.TransactionType;
import com.monst.bankingplugin.exceptions.WorldNotFoundException;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Callback;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {
	private final Set<String> notFoundWorlds = new HashSet<>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String tableBanks;
	String tableAccounts;
	String tableTransactionLog;
	String tableInterestLog;
	String tableLogouts;
	String tableFields;

	BankingPlugin plugin;
	HikariDataSource dataSource;

	protected Database(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	abstract HikariDataSource getDataSource();

	abstract String getQueryCreateTableBanks();

	abstract String getQueryCreateTableAccounts();

	abstract String getQueryCreateTableTransactionLog();

	abstract String getQueryCreateTableInterestLog();

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

	private boolean update() throws SQLException {
		String queryGetTable = getQueryGetTable();

		try (Connection con = dataSource.getConnection()) {
			boolean needsUpdate1 = false; // update "transaction_log" to "economy_logs" and update "accounts" with
											// prefixes
			boolean needsUpdate2 = false; // create field table and set database version

			try (PreparedStatement ps = con.prepareStatement(queryGetTable)) {
				ps.setString(1, "account_log");
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					needsUpdate1 = true;
				}
			}

			try (PreparedStatement ps = con.prepareStatement(queryGetTable)) {
				ps.setString(1, tableFields);
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					needsUpdate2 = true;
				}
			}

			if (needsUpdate1) {
				String queryRenameTableBanks = "ALTER TABLE banks RENAME TO backup_banks"; // for backup
				String queryRenameTableAccounts = "ALTER TABLE accounts RENAME TO backup_accounts"; // for backup
				String queryRenameTableTransactionLog = "ALTER TABLE transaction_log RENAME TO backup_transaction_log"; // for backup
				String queryRenameTableInterestLog = "ALTER TABLE interest_log RENAME TO backup_interest_log"; // for backup
				String queryRenameTableLogouts = "ALTER TABLE player_logout RENAME TO " + tableLogouts;

				plugin.getLogger().info("Updating database... (#1)");

				// Rename banks table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(queryRenameTableBanks);
				}

				// Rename accounts table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(queryRenameTableAccounts);
				}

				// Rename transaction log table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(queryRenameTableTransactionLog);
				}

				// Rename interest log table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(queryRenameTableInterestLog);
				}

				// Rename logout table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(queryRenameTableLogouts);
				}

				// Create new banks table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(getQueryCreateTableBanks());
				}

				// Create new accounts table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(getQueryCreateTableAccounts());
				}

				// Create new transaction log table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(getQueryCreateTableTransactionLog());
				}

				// Create new interest log table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(getQueryCreateTableInterestLog());
				}

				// TODO: Check table conversion, make sure extra steps in ShopChest unnecessary for this
				// TODO: Recheck queries, practically all tables have changed

				// Convert banks table
				try (Statement s = con.createStatement()) {
					ResultSet rs = s.executeQuery("SELECT id FROM backup_banks");
					while (rs.next()) {

						String insertQuery = "INSERT INTO " + tableBanks
								+ " SELECT id,name,world,selection_type,minY,maxY,points FROM backup_banks"
								+ " WHERE id = ?";
						try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
							ps.setInt(1, rs.getInt("id"));
							ps.executeUpdate();
						}
					}
				}

				// Convert accounts table
				try (Statement s = con.createStatement()) {
					ResultSet rs = s.executeQuery("SELECT id FROM backup_accounts");
					while (rs.next()) {

						String insertQuery = "INSERT INTO " + tableAccounts
								+ " SELECT id,bank_id,owner,size,balance,prev_balance,multiplier_stage,"
								+ "until_payout,remaining_offline_payout,remaining_offline_until_reset,"
								+ "world,x,y,z FROM backup_accounts WHERE id = ?";
						try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
							ps.setInt(1, rs.getInt("id"));
							ps.executeUpdate();
						}
					}
				}

				// Convert transaction log table
				try (Statement s = con.createStatement()) {
					ResultSet rs = s.executeQuery(
							"SELECT id FROM backup_transaction_log");
					while (rs.next()) {
						
						String timestamp = rs.getString("timestamp");
						long time = 0L;
						try {
							time = dateFormat.parse(timestamp).getTime();
						} catch (ParseException e) {
							plugin.debug("Failed to parse timestamp '" + timestamp + "': Time is set to 0");
							plugin.debug(e);
						}

						String insertQuery = "INSERT INTO " + tableTransactionLog
								+ " SELECT id,account_id,bank_id,timestamp,time,owner_name,"
								+ "owner_uuid,executor_name,executor_uuid,transaction_type,"
								+ "amount,new_balance,world,x,y,z FROM backup_transaction_log WHERE id = ?";
						try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
							ps.setInt(1, rs.getInt("id"));
							ps.executeUpdate();
						}
					}
				}
				
				// Convert interest log table
				try (Statement s = con.createStatement()) {
					ResultSet rs = s.executeQuery("SELECT id FROM backup_interest_log");
					while (rs.next()) {

						String insertQuery = "INSERT INTO " + tableInterestLog
								+ " SELECT id,account_id,bank_id,owner_name,owner_uuid,base_amount,"
								+ "multiplier,amount,timestamp,time FROM backup_interest_log WHERE id = ?";
						try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
							ps.setInt(1, rs.getInt("id"));
							ps.executeUpdate();
						}
					}
				}
			}

			if (needsUpdate2) {
				plugin.getLogger().info("Updating database... (#2)");

				// Create fields table
				try (Statement s = con.createStatement()) {
					s.executeUpdate(getQueryCreateTableFields());
				}

				setDatabaseVersion(2);
			}

			int databaseVersion = getDatabaseVersion();

			if (databaseVersion < 3) {
				// plugin.getLogger().info("Updating database... (#3)");

				// Update database structure...

				// setDatabaseVersion(3);
			}

			int newDatabaseVersion = getDatabaseVersion();
			return needsUpdate1 || needsUpdate2 || newDatabaseVersion > databaseVersion;
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
	 * @param callback Callback that - if succeeded - returns the amount of shops
	 *                 that were found (as {@code int})
	 */
	public void connect(final Callback<Integer[]> callback) {
		if (!Config.databaseTablePrefix.matches("^([a-zA-Z0-9\\-\\_]+)?$")) {
			// Only letters, numbers dashes and underscores are allowed
			plugin.getLogger().severe("Database table prefix contains illegal letters, using 'bankingplugin_' prefix.");
			Config.databaseTablePrefix = "bankingplugin_";
		}

		this.tableBanks = Config.databaseTablePrefix + "banks";
		this.tableAccounts = Config.databaseTablePrefix + "accounts";
		this.tableTransactionLog = Config.databaseTablePrefix + "transaction_log";
		this.tableInterestLog = Config.databaseTablePrefix + "interest_log";
		this.tableLogouts = Config.databaseTablePrefix + "player_logouts";
		this.tableFields = Config.databaseTablePrefix + "fields";

		new BukkitRunnable() {
			@Override
			public void run() {
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
						plugin.getLogger().info("Updating database finished");
					}

					// Create banks table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableBanks());
					}

					// Create accounts table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableAccounts());
					}

					// Create transaction log table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableTransactionLog());
					}

					// Create interest log table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableInterestLog());
					}

					// Create logout table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableLogout());
					}

					// Create fields table
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableFields());
					}

					// Clean up economy log
					if (Config.cleanupLogDays > 0) {
						cleanUpLogs(false);
					}

					// Count accounts entries in database
					try (Statement s = con.createStatement()) {
						Integer accounts;
						ResultSet rsAccounts = s.executeQuery("SELECT COUNT(id) FROM " + tableAccounts);
						if (rsAccounts.next()) {
							accounts = rsAccounts.getInt(1);

							plugin.debug("Initialized database with " + accounts + " account entries");

						} else {
							throw new SQLException("Count result set has no account entries");
						}

						Integer banks;
						ResultSet rsBanks = s.executeQuery("SELECT COUNT(id) FROM " + tableBanks);
						if (rsBanks.next()) {
							banks = rsBanks.getInt(1);

							plugin.debug("Initialized database with " + banks + " bank entries");

						} else {
							throw new SQLException("Count result set has no bank entries");
						}

						if (callback != null) {
							callback.callSyncResult(new Integer[] { accounts, banks });
						}
					}
				} catch (SQLException e) {
					if (callback != null) {
						callback.callSyncError(e);
					}

					plugin.getLogger().severe("Failed to initialize or connect to database");
					plugin.debug("Failed to initialize or connect to database");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
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
				+ " (bank_id,owner,size,balance,prev_balance,multiplier_stage,offline_payout_remaining,until_payout,world,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		final String queryWithId = "REPLACE INTO " + tableAccounts
				+ " (id,bank_id,owner,size,balance,prev_balance,multiplier_stage,offline_payout_remaining,until_payout,world,x,y,z) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		new BukkitRunnable() {
			@Override
			public void run() {
				String query = account.hasID() ? queryWithId : queryNoId;

				try (Connection con = dataSource.getConnection();
						PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
					int i = 0;
					if (account.hasID()) {
						ps.setInt(1, account.getID());
						i++;
					}

					ps.setInt(i + 1, account.getBank().getID());
					ps.setString(i + 2, account.getOwner().getUniqueId().toString());
					ps.setInt(i + 3, account.getChestSize());

					ps.setString(i + 4, account.getBalance().toString());
					ps.setString(i + 5, account.getPrevBalance().toString());

					ps.setInt(i + 6, account.getStatus().getMultiplierStage());
					ps.setInt(i + 7, account.getStatus().getRemainingOfflinePayouts());
					ps.setInt(i + 8, account.getStatus().getRemainingUntilFirstPayout());

					ps.setString(i + 3, account.getLocation().getWorld().getName());
					ps.setInt(i + 6, account.getLocation().getBlockX());
					ps.setInt(i + 7, account.getLocation().getBlockY());
					ps.setInt(i + 8, account.getLocation().getBlockZ());

					ps.executeUpdate();

					if (!account.hasID()) {
						int accountId = -1;
						ResultSet rs = ps.getGeneratedKeys();
						if (rs.next()) {
							accountId = rs.getInt(1);
						}

						account.setID(accountId);
					}

					if (callback != null) {
						// What does this do?
						callback.callSyncResult(account.getID());
					}

					plugin.debug("Adding account to database (#" + account.getID() + ")");
				} catch (SQLException e) {
					if (callback != null) {
						callback.callSyncError(e);
					}

					plugin.getLogger().severe("Failed to account to database (#" + account.getID() + ")");
					plugin.debug("Failed to add account to database (#" + account.getID() + ")");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Remove an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(final Account account, final Callback<Void> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
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

					plugin.getLogger().severe("Failed to remove account from database (#" + account.getID() + ")");
					plugin.debug("Failed to remove account from database (#" + account.getID() + ")");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Get all accounts from the database
	 * 
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all accounts (as
	 *                            {@code Collection<Account>})
	 */
	public void getAccounts(final boolean showConsoleMessages, final Callback<Collection<Account>> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<Account> accounts = new ArrayList<>();

				try (Connection con = dataSource.getConnection();
						PreparedStatement psAccounts = con.prepareStatement("SELECT * FROM " + tableAccounts + "")) {
					ResultSet rsAccounts = psAccounts.executeQuery();

					while (rsAccounts.next()) {

						int accountId = rsAccounts.getInt("id");

						plugin.debug("Getting account from database... (#" + accountId + ")");

						String worldName = rsAccounts.getString("world");
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

						Bank bank = null;
						int bankId = rsAccounts.getInt("bank_id");
						BankUtils bankUtils = plugin.getBankUtils();
						bank = bankUtils.getBankByID(bankId);
						if (bank == null) {
							plugin.debug("Failed to associate bank with account (#" + accountId + ")");
							continue;
						}

						AccountStatus status;
						try {
							int multiplierStage = rsAccounts.getInt("multiplier_stage");
							int remainingUntilPayout = rsAccounts.getInt("remaining_until_payout");
							int remainingOfflinePayouts = rsAccounts.getInt("remaining_offline_payouts");
							int remainingOfflineUntilReset = rsAccounts.getInt("remaining_offline_until_reset");

							BigDecimal balance = BigDecimal
									.valueOf(Double.parseDouble(rsAccounts.getString("balance")));
							BigDecimal prevBalance = BigDecimal
									.valueOf(Double.parseDouble(rsAccounts.getString("prev_balance")));

							status = new AccountStatus(multiplierStage, remainingUntilPayout, remainingOfflinePayouts,
									remainingOfflineUntilReset, balance, prevBalance);

						} catch (SQLException e) {
							plugin.getLogger().severe("Failed to create account status (#" + accountId + ")");
							plugin.debug("Failed to create account status");
							plugin.debug(e);
							continue;
						}

						int x = rsAccounts.getInt("x");
						int y = rsAccounts.getInt("y");
						int z = rsAccounts.getInt("z");
						Location location = new Location(world, x, y, z);
						OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(rsAccounts.getString("owner")));

						plugin.debug("Initializing account... (#" + accountId + ")");

						Account account = new Account(accountId, plugin, owner, bank, location, status);
						accounts.add(account);
						bank.addAccount(account);
					}

					if (callback != null) {
						callback.callSyncResult(Collections.unmodifiableCollection(accounts));
					}
				} catch (SQLException e) {
					if (callback != null) {
						callback.callSyncError(e);
					}

					plugin.getLogger().severe("Failed to get accounts from database");
					plugin.debug("Failed to get accounts from database");
					plugin.debug(e);
				}

			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Add a bank to the database
	 * 
	 * @param bank     Bank to add
	 * @param callback Callback that - if succeeded - returns the ID the bank was
	 *                 given (as {@code int})
	 */
	public void addBank(final Bank bank, final Callback<Integer> callback) {
		final String queryNoId = "REPLACE INTO " + tableBanks 
				+ " (id,name,world,selection_type,minY,maxY,points) VALUES(?,?,?,?,?,?,?)";
		final String queryWithId = "REPLACE INTO " + tableBanks
				+ " (name,world,selection_type,minY,maxY,points) VALUES(?,?,?,?,?,?)";

		new BukkitRunnable() {
			@Override
			public void run() {
				String query = bank.hasID() ? queryWithId : queryNoId;

				try (Connection con = dataSource.getConnection();
						PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
					int i = 0;
					if (bank.hasID()) {
						i = 1;
						ps.setInt(1, bank.getID());
					}

					ps.setString(i + 1, bank.getName());
					ps.setString(i + 2, bank.getWorld().getName());
					ps.setString(i + 3, bank.getSelectionType());

					if (bank.getSelection() instanceof Polygonal2DSelection) {
						Polygonal2DSelection sel = (Polygonal2DSelection) bank.getSelection();

						ps.setInt(i + 4, sel.getMaximumPoint().getBlockY());
						ps.setInt(i + 5, sel.getMinimumPoint().getBlockY());

						String vertices = sel.getNativePoints().stream()
								.map(vector -> "" + vector.getBlockX() + "," + vector.getBlockZ())
								.collect(Collectors.joining(" | "));

						ps.setString(i + 6, vertices);

					} else if (bank.getSelection() instanceof CuboidSelection) {
						CuboidSelection sel = (CuboidSelection) bank.getSelection();

						StringBuilder sb = new StringBuilder();
						Location max = sel.getMaximumPoint();
						Location min = sel.getMinimumPoint();

						sb.append(max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ());
						sb.append(" | ");
						sb.append(min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ());

						ps.setString(i + 6, sb.toString());
					} else {
						plugin.getLogger()
								.severe("Bank selection neither cuboid nor polygonal! (#" + bank.getID() + ")");
						plugin.debug("Bank selection neither cuboid nor polygonal! (#" + bank.getID() + ")");
					}

					ps.executeUpdate();

					if (!bank.hasID()) {
						int accountId = -1;
						ResultSet rs = ps.getGeneratedKeys();
						if (rs.next()) {
							accountId = rs.getInt(1);
						}

						bank.setID(accountId);
					}

					if (callback != null) {
						callback.callSyncResult(bank.getID());
					}

					plugin.debug("Adding bank to database (#" + bank.getID() + ")");
				} catch (SQLException e) {
					if (callback != null) {
						callback.callSyncError(e);
					}

					plugin.getLogger().severe("Failed to add bank to database");
					plugin.debug("Failed to add bank to database (#" + bank.getID() + ")");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Remove a bank from the database
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(final Bank bank, final Callback<Void> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
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

					plugin.getLogger().severe("Failed to remove account from database (#" + bank.getID() + ")");
					plugin.debug("Failed to remove account from database (#" + bank.getID() + ")");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Get all banks from the database
	 * 
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all banks (as
	 *                            {@code Collection<Account>})
	 */
	public void getBanks(final boolean showConsoleMessages, final Callback<Collection<Bank>> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ArrayList<Bank> banks = new ArrayList<>();

				try (Connection con = dataSource.getConnection();
						PreparedStatement psBanks = con.prepareStatement("SELECT * FROM " + tableBanks + "")) {
					// id,name,world,selection_type,minY,maxY,points
					ResultSet rsBanks = psBanks.executeQuery();

					while (rsBanks.next()) {

						int bankId = rsBanks.getInt("id");

						plugin.debug("Getting bank from database... (#" + bankId + ")");

						String worldName = rsBanks.getString("world");
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

						String name = rsBanks.getString("name");
						
						Selection selection;
						String[] pointArray = rsBanks.getString("points").split(" \\| ");
						
						if (rsBanks.getString("selection_type").equals("POLYGONAL")) {
							int minY = rsBanks.getInt("minY");
							int maxY = rsBanks.getInt("maxY");
							
							List<BlockVector2D> nativePoints = new ArrayList<>();
							
							for (String point : pointArray) {
								String[] coords = point.split(",");
								int x = Integer.parseInt(coords[0]);
								int z = Integer.parseInt(coords[1]);
								nativePoints.add(new BlockVector2D(x, z));
							}
							
							selection = new Polygonal2DSelection(world, nativePoints, minY, maxY);
						} else {
							
							String[] coords = pointArray[0].split(",");
							int minX = Integer.parseInt(coords[0]);
							int minY = Integer.parseInt(coords[1]);
							int minZ = Integer.parseInt(coords[2]);
							
							coords = pointArray[1].split(",");
							int maxX = Integer.parseInt(coords[0]);
							int maxY = Integer.parseInt(coords[1]);
							int maxZ = Integer.parseInt(coords[2]);
							
							Location min = new Location(world, minX, minY, minZ);
							Location max = new Location(world, maxX, maxY, maxZ);
							
							selection = new CuboidSelection(world, min, max);
						}

						plugin.debug("Initializing bank... (#" + bankId + ")");

						Bank bank = new Bank(bankId, plugin, name, selection);

						banks.add(bank);
					}

					if (callback != null) {
						callback.callSyncResult(Collections.unmodifiableCollection(banks));
					}
				} catch (SQLException e) {
					if (callback != null) {
						callback.callSyncError(e);
					}

					plugin.getLogger().severe("Failed to get banks from database");
					plugin.debug("Failed to get banks from database");
					plugin.debug(e);
				}

			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Log an economy transaction to the database
	 * 
	 * @param executor Player who performed a transaction
	 * @param account  The {@link Account} the player performed the transaction on
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param type     Whether the executor deposited or withdrew
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logTransaction(final Player executor, Account account, BigDecimal amount, TransactionType type,
			final Callback<Void> callback) {
		final String query = "INSERT INTO " + tableTransactionLog
				+ " (account_id,bank_id,timestamp,time,owner_name,owner_uuid,executor_name,executor_uuid,transaction_type,amount,new_balance,world,x,y,z)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		if (Config.enableTransactionLog) {
			new BukkitRunnable() {
				@Override
				public void run() {
					try (Connection con = dataSource.getConnection();
							PreparedStatement ps = con.prepareStatement(query)) {

						long millis = System.currentTimeMillis();

						ps.setInt(1, account.getID());
						ps.setInt(2, account.getBank().getID());
						ps.setString(3, dateFormat.format(millis));
						ps.setLong(4, millis);
						ps.setString(5, account.getOwner().getName());
						ps.setString(6, account.getOwner().getUniqueId().toString());
						if (!account.getOwner().getUniqueId().equals(executor.getUniqueId())) {
							ps.setString(7, executor.getName());
							ps.setString(8, executor.getUniqueId().toString());
						} else {
							ps.setString(7, "OWNER");
						}
						ps.setString(9, type.toString());
						ps.setString(10, amount.toString());
						ps.setString(11, account.getBalance().toString());
						ps.setString(12, account.getLocation().getWorld().getName());
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

						plugin.getLogger().severe("Failed to log banking transaction to database");
						plugin.debug("Failed to log banking transaction to database");
						plugin.debug(e);
					}
				}
			}.runTaskAsynchronously(plugin);
		} else {
			if (callback != null) {
				callback.callSyncResult(null);
			}
		}
	}

	/**
	 * Log an interest payout to the database
	 * 
	 * @param account  The {@link Account} the interest was derived from
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param type     Whether the executor deposited or withdrew
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logInterest(Account account, BigDecimal baseAmount, int multiplier, BigDecimal amount,
			final Callback<Void> callback) {
		final String query = "INSERT INTO " + tableInterestLog
				+ " (account_id,bank_id,owner_name,owner_uuid,base_amount,multiplier,amount,timestamp,time)"
				+ " VALUES(?,?,?,?,?,?,?,?,?)";

		if (Config.enableInterestLog) {
			new BukkitRunnable() {
				@Override
				public void run() {
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

						plugin.debug("Logged transaction to database");
					} catch (SQLException e) {
						if (callback != null) {
							callback.callSyncError(e);
						}

						plugin.getLogger().severe("Failed to log banking transaction to database");
						plugin.debug("Failed to log banking transaction to database");
						plugin.debug(e);
					}
				}
			}.runTaskAsynchronously(plugin);
		} else {
			if (callback != null) {
				callback.callSyncResult(null);
			}
		}
	}

	/**
	 * Cleans up the economy log to reduce file size
	 * 
	 * @param async Whether the call should be executed asynchronously
	 */
	public void cleanUpLogs(boolean async) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
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

					plugin.getLogger().info("Cleaned up banking logs");
					plugin.debug("Cleaned up banking logs");
				} catch (SQLException e) {
					plugin.getLogger().severe("Failed to clean up banking logs");
					plugin.debug("Failed to clean up banking logs");
					plugin.debug(e);
				}
			}
		};

		if (async) {
			runnable.runTaskAsynchronously(plugin);
		} else {
			runnable.run();
		}
	}

    /**
     * Log a logout to the database
     * 
     * @param player    Player who logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(final Player player, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableLogouts + " (player,time) VALUES(?,?)")) {

                    ps.setString(1, player.getUniqueId().toString());
                    ps.setLong(2, System.currentTimeMillis());
                    ps.executeUpdate();

                    if (callback != null) {
                        callback.callSyncResult(null);
                    }

                    plugin.debug("Logged logout to database");
                } catch (final SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to log last logout to database");
                    plugin.debug("Failed to log logout to database");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get the revenue a player got while he was offline
     * 
     * @param player     Player whose revenue to get
     * @param logoutTime Time in milliseconds when he logged out the last time
     * @param callback   Callback that - if succeeded - returns the revenue the
     *                   player made while offline (as {@code double})
     */
	public void getOfflineInterestRevenue(final Player player, final long logoutTime,
			final Callback<BigDecimal> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
				BigDecimal revenue = BigDecimal.ZERO;

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableInterestLog + " WHERE owner_uuid = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        long interestTime = rs.getLong("time");
                        if (interestTime > logoutTime) {
							BigDecimal interest = BigDecimal.valueOf(Double.parseDouble(rs.getString("amount")));
							revenue = revenue.add(interest);
                        }
                    }
					revenue = revenue.setScale(2, RoundingMode.HALF_EVEN);

                    if (callback != null) {
						callback.callSyncResult(revenue);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get revenue from database");
                    plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\"");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }


	/**
	 * Get the revenue a player got while he was offline
	 * 
	 * @param player     Player whose revenue to get
	 * @param logoutTime Time in milliseconds when he logged out the last time
	 * @param callback   Callback that - if succeeded - returns the revenue the
	 *                   player made while offline (as {@code double})
	 */
	public void getOfflineTransactionRevenue(final Player player, final long logoutTime,
			final Callback<BigDecimal> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				BigDecimal revenue = BigDecimal.ZERO;
				
				try (Connection con = dataSource.getConnection();
						PreparedStatement ps = con
								.prepareStatement("SELECT * FROM " + tableTransactionLog + " WHERE owner_uuid = ?")) {
					ps.setString(1, player.getUniqueId().toString());
					ResultSet rs = ps.executeQuery();
					
					while (rs.next()) {
						long interestTime = rs.getLong("time");
						if (interestTime > logoutTime) {
							BigDecimal interest = BigDecimal.valueOf(Double.parseDouble(rs.getString("amount")));
							revenue = revenue.add(interest);
						}
					}
					revenue = revenue.setScale(2, RoundingMode.HALF_EVEN);
					
					if (callback != null) {
						callback.callSyncResult(revenue);
					}
				} catch (SQLException ex) {
					if (callback != null) {
						callback.callSyncError(ex);
					}
					
					plugin.getLogger().severe("Failed to get revenue from database");
					plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\"");
					plugin.debug(ex);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Get the last logout of a player
	 * 
	 * @param player   Player who logged out
	 * @param callback Callback that - if succeeded - returns the time in
	 *                 milliseconds the player logged out (as {@code long}) or
	 *                 {@code -1} if the player has not logged out yet.
	 */
	public void getLastLogout(final Player player, final Callback<Long> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
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

					plugin.getLogger().severe("Failed to get last logout from database");
					plugin.debug("Failed to get last logout from player \"" + player.getName() + "\"");
					plugin.debug(e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Closes the data source
	 */
	public void disconnect() {
		if (dataSource != null) {
			dataSource.close();
			dataSource = null;
		}
	}

}