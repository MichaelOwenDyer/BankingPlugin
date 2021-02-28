package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankConfig;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.WorldNotFoundException;
import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.selections.*;
import com.monst.bankingplugin.geo.selections.Selection.SelectionType;
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
import java.util.stream.Collectors;

public abstract class Database {

	private final Set<String> notFoundWorlds = new HashSet<>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String tableBanks;
	String tableCoOwnsBank;
	String tableAccounts;
	String tableCoOwnsAccount;
	String tableAccountTransactions;
	String tableAccountInterest;
	String tableBankRevenue;
	String tableLowBalanceFees;
	String tablePlayers;
	String tableFields;

	final BankingPlugin plugin;
	HikariDataSource dataSource;

	Database(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	abstract HikariDataSource getDataSource();

	abstract String getQueryCreateTable(String tableName, String... columns);

	abstract String getQueryCreateTableBanks();

	abstract String getQueryCreateTableCoOwnsBank();

	abstract String getQueryCreateTableAccounts();

	abstract String getQueryCreateTableCoOwnsAccount();

	abstract String getQueryCreateTableAccountTransactions();

	abstract String getQueryCreateTableAccountInterest();

	abstract String getQueryCreateTableBankRevenue();

	abstract String getQueryCreateTableLowBalanceFees();

	abstract String getQueryCreateTablePlayers();

	abstract String getQueryCreateTableFields();

	private int getDatabaseVersion() throws SQLException {
		try (ResultSet rs = dataSource.getConnection().createStatement()
				.executeQuery("SELECT Value FROM " + tableFields + " WHERE Field = 'Version'")) {
			if (rs.next())
				return rs.getInt(1);
		}
		return 0;
	}

	private void setDatabaseVersion(int version) throws SQLException {
		try (Connection con = dataSource.getConnection();
			 PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableFields + " VALUES ('Version', ?)")) {
			ps.setInt(1, version);
			ps.executeUpdate();
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

		this.tableBanks = "Banks";
		this.tableCoOwnsBank = "co_owns_bank";
		this.tableAccounts = "Accounts";
		this.tableCoOwnsAccount = "co_owns_account";
		this.tableAccountTransactions = "AccountTransactions";
		this.tableAccountInterest = "AccountInterest";
		this.tableBankRevenue = "BankRevenue";
		this.tableLowBalanceFees = "LowBalanceFees";
		this.tablePlayers = "Players";
		this.tableFields = "Fields";

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
				if (update())
					plugin.getLogger().info("Updating database finished.");

				plugin.debug("Starting table creation");

				try (Statement s = con.createStatement()) {
					for (String query : new String[] {
							getQueryCreateTableBanks(), // Create banks table
							getQueryCreateTableCoOwnsBank(), // Create co_owns_bank table
							getQueryCreateTableAccounts(), // Create accounts table
							getQueryCreateTableCoOwnsAccount(), // Create co_owns_account table
							getQueryCreateTableAccountTransactions(), // Create account transaction log table
							getQueryCreateTableAccountInterest(), // Create account interest log table
							getQueryCreateTableBankRevenue(), // Create bank revenue log table
							getQueryCreateTableLowBalanceFees(), // Create low balance fee log table
							getQueryCreateTablePlayers(), // Create players table
							getQueryCreateTableFields() // Create fields table
					})
						s.executeUpdate(query);
				}

				// Clean up economy log
				if (Config.cleanupLogDays > 0)
					cleanUpLogs(false);

				// Count accounts entries in database
				try (Statement s = con.createStatement()) {
					int accounts;
					ResultSet rsAccounts = s.executeQuery("SELECT COUNT(AccountID) FROM " + tableAccounts);
					if (rsAccounts.next())
						accounts = rsAccounts.getInt(1);
					else
						throw new SQLException("Count result set has no account entries");
					rsAccounts.close();

					int banks;
					ResultSet rsBanks = s.executeQuery("SELECT COUNT(BankID) FROM " + tableBanks);
					if (rsBanks.next())
						banks = rsBanks.getInt(1);
					else
						throw new SQLException("Count result set has no bank entries");
					rsBanks.close();

					if (callback != null)
						callback.callSyncResult(new int[] {banks, accounts});

				}
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to initialize or connect to database.");
				plugin.debug("Failed to initialize or connect to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Carry out any potential updates on the database structure if necessary.
	 * @return whether the database was updated or not
	 * @throws SQLException if there was an error updating the database
	 */
	private boolean update() throws SQLException {
		return false;
	}

	/**
	 * Adds an account to the database
	 *
	 * @param account  Account to add
	 * @param callback Callback that - if succeeded - returns the ID the account was
	 *                 given (as {@code int})
	 */
	public void addAccount(final Account account, final Callback<Integer> callback) {
		final String query = "REPLACE INTO " + tableAccounts + " (" +
				(account.hasID() ? "AccountID, " : "") + "BankID, Nickname, OwnerUUID, Balance, PreviousBalance, MultiplierStage, " +
				"DelayUntilNextPayout, RemainingOfflinePayouts, RemainingOfflinePayoutsUntilReset, World, x, y, z)" +
				"VALUES(" + (account.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				if (account.hasID())
					values.addNext(account.getID());
				values.addNext(account.getBank().getID());
				values.addNext(account.getRawName());
				values.addNext(account.getOwner().getUniqueId().toString());
				values.addNext(account.getBalance().toString());
				values.addNext(account.getPrevBalance().toString());
				values.addNext(account.getMultiplierStage());
				values.addNext(account.getDelayUntilNextPayout());
				values.addNext(account.getRemainingOfflinePayouts());
				values.addNext(account.getRemainingOfflinePayoutsUntilReset());
				values.addNext(account.getLocation().getWorld().getName());
				values.addNext(account.getLocation().getBlockX());
				values.addNext(account.getLocation().getBlockY());
				values.addNext(account.getLocation().getBlockZ());

				ps.executeUpdate();

				if (!account.hasID()) {
					int accountId = -1;
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next())
						accountId = rs.getInt(1);
					rs.close();
					account.setID(accountId);
				}

				account.getBank().addAccount(account);

				for (OfflinePlayer coowner : account.getCoOwners())
					addCoowner(account, coowner, null, con);

				if (callback != null)
					callback.callSyncResult(account.getID());

				plugin.debugf("Added account to database (#%d).", account.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe(String.format("Failed to add account to database (#%d).", account.getID()));
				plugin.debugf("Failed to add account to database (#%d).", account.getID());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Removes an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(final Account account, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
					PreparedStatement ps = con
							.prepareStatement("DELETE FROM " + tableAccounts + " WHERE AccountID = ?")) {

				ps.setInt(1, account.getID());

				ps.executeUpdate();

				removeCoowner(account, null, null, con);

				if (callback != null)
					callback.callSyncResult(null);

				plugin.debugf("Removing account from database (#%d)", account.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe(String.format("Failed to remove account from database (#%d).", account.getID()));
				plugin.debugf("Failed to remove account from database (#%d).", account.getID());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Adds a bank to the database
	 *
	 * @param bank     Bank to add
	 * @param callback Callback that - if succeeded - returns the ID the bank was
	 *                 given (as {@code int})
	 */
	public void addBank(final Bank bank, final Callback<Integer> callback) {
		final String query = "REPLACE INTO " + tableBanks + " (" +
				(bank.hasID() ? "BankID, " : "") + "Name, OwnerUUID, CountInterestDelayOffline, ReimburseAccountCreation, " +
				"PayOnLowBalance, InterestRate, AccountCreationPrice, MinimumBalance, LowBalanceFee, InitialInterestDelay, " +
				"AllowedOfflinePayouts, AllowedOfflinePayoutsBeforeMultiplierReset, OfflineMultiplierDecrement, " +
				"WithdrawalMultiplierDecrement, PlayerBankAccountLimit, Multipliers, InterestPayoutTimes, " +
				"World, MinX, MaxX, MinY, MaxY, MinZ, MaxZ, PolygonVertices) " +
				"VALUES(" + (bank.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				if (bank.hasID())
					values.addNext(bank.getID());
				values.addNext(bank.getRawName());
				values.addNext(bank.isPlayerBank() ? bank.getOwner().getUniqueId().toString() : null);
				values.addNext((boolean) bank.get(BankField.COUNT_INTEREST_DELAY_OFFLINE));
				values.addNext((boolean) bank.get(BankField.REIMBURSE_ACCOUNT_CREATION));
				values.addNext((boolean) bank.get(BankField.PAY_ON_LOW_BALANCE));
				values.addNext((double) bank.get(BankField.INTEREST_RATE));
				values.addNext((double) bank.get(BankField.ACCOUNT_CREATION_PRICE));
				values.addNext((double) bank.get(BankField.MINIMUM_BALANCE));
				values.addNext((double) bank.get(BankField.LOW_BALANCE_FEE));
				values.addNext((int) bank.get(BankField.INITIAL_INTEREST_DELAY));
				values.addNext((int) bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS));
				values.addNext((int) bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET));
				values.addNext((int) bank.get(BankField.OFFLINE_MULTIPLIER_DECREMENT));
				values.addNext((int) bank.get(BankField.WITHDRAWAL_MULTIPLIER_DECREMENT));
				values.addNext((int) bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT));
				values.addNext((List<?>) bank.get(BankField.MULTIPLIERS));
				values.addNext((List<?>) bank.get(BankField.INTEREST_PAYOUT_TIMES));
				values.addNext(bank.getSelection().getWorld().getName());
				values.addNext(bank.getSelection().getMinX());
				values.addNext(bank.getSelection().getMaxX());
				values.addNext(bank.getSelection().getMinY());
				values.addNext(bank.getSelection().getMaxY());
				values.addNext(bank.getSelection().getMinZ());
				values.addNext(bank.getSelection().getMaxZ());
				values.addNext(bank.getSelection().getType() == SelectionType.POLYGONAL ?
						((PolygonalSelection) bank.getSelection()).getVertices() : null);

				ps.executeUpdate();

				if (!bank.hasID()) {
					int bankId = -1;
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next())
						bankId = rs.getInt(1);
					rs.close();
					bank.setID(bankId);
				}

				for (OfflinePlayer coowner : bank.getCoOwners())
					addCoowner(bank, coowner, null, con);

				if (callback != null)
					callback.callSyncResult(bank.getID());

				plugin.debugf("Adding bank to database (#%d)", bank.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to add bank to database.");
				plugin.debugf("Failed to add bank to database (#%d)", bank.getID());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Removes a bank from the database. All accounts associated with this bank will automatically be removed from the database as well.
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(final Bank bank, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con
						 .prepareStatement("DELETE FROM " + tableBanks + " WHERE BankID = ?")) {

				ps.setInt(1, bank.getID());

				ps.executeUpdate();

				removeCoowner(bank, null, null, con);

				if (callback != null)
					callback.callSyncResult(null);

				plugin.debugf("Removing bank from database (#%d)", bank.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe(String.format("Failed to remove bank from database (#%d).", bank.getID()));
				plugin.debugf("Failed to remove bank from database (#%d).", bank.getID());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Adds a co-owner to the database. This method runs asynchronously and should be called from a synchronous context!
	 * @param ownable ownable the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoowner(final BankingEntity ownable, final OfflinePlayer coowner, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection()) {
				addCoowner(ownable, coowner, callback, con);
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe(String.format("Failed to add co-owner to database (#%d, UUID: %s).", ownable.getID(), coowner.getUniqueId().toString()));
				plugin.debugf("Failed to add co-owner to database (#%d, UUID: %s). Connection failed.", ownable.getID(), coowner.getUniqueId().toString());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Adds a co-owner to the database. This method runs synchronously and must be called from an asynchronous context!
	 * @param ownable ownable the player co-owns
	 * @param coowner the co-owner to be added
	 */
	private void addCoowner(final BankingEntity ownable, final OfflinePlayer coowner, final Callback<Void> callback, final Connection con) {
		final String query;
		if (ownable instanceof Account)
			query = "REPLACE INTO " + tableCoOwnsAccount + " (CoOwnerUUID, AccountID) VALUES(?,?)";
		else
			query = "REPLACE INTO " + tableCoOwnsBank + " (CoOwnerUUID, BankID) VALUES(?,?)";
		try (PreparedStatement ps = con.prepareStatement(query)) {

			ps.setString(1, coowner.getUniqueId().toString());
			ps.setInt(2, ownable.getID());

			ps.executeUpdate();

			if (callback != null)
				callback.callSyncResult(null);

			plugin.debugf("Added co-owner to database (#%d, UUID: %s).", ownable.getID(), coowner.getUniqueId().toString());
		} catch (SQLException e) {
			if (callback != null)
				callback.callSyncError(e);
			plugin.getLogger().severe(String.format("Failed to add co-owner to database (#%d, UUID: %s).", ownable.getID(), coowner.getUniqueId().toString()));
			plugin.debugf("Failed to add co-owner to database (#%d, UUID: %s). PreparedStatement failed.", ownable.getID(), coowner.getUniqueId().toString());
			plugin.debug(e);
		}
	}

	/**
	 * Removes a co-owner from the database. This method runs asynchronously and should be called from a synchronous context!
	 * @param ownable ownable the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 */
	public void removeCoowner(final BankingEntity ownable, final OfflinePlayer coowner, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection()) {
				removeCoowner(ownable, coowner, callback, con);
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe(String.format("Failed to remove co-owner(s) from database (#%d).", ownable.getID()));
				plugin.debugf("Failed to remove co-owner(s) from database (#%d). Connection failed.", ownable.getID());
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Removes a co-owner from the database. This method runs synchronously and must be called from an asynchronous context!
	 * @param ownable ownable the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 */
	private void removeCoowner(final BankingEntity ownable, final OfflinePlayer coowner, final Callback<Void> callback, final Connection con) {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		if (ownable instanceof Account)
			query.append(tableCoOwnsAccount).append(" WHERE AccountID = ");
		else
			query.append(tableCoOwnsBank).append(" WHERE BankID = ");
		query.append(ownable.getID());
		if (coowner != null)
			query.append(" AND CoOwnerUUID = '").append(coowner.getUniqueId().toString()).append("'");

		try (Statement ps = con.createStatement()) {

			int affectedTables = ps.executeUpdate(query.toString());

			if (callback != null)
				callback.callSyncResult(null);

			if (affectedTables != 0)
				plugin.debugf("Removed co-owner(s) from database (#%d).", ownable.getID());
		} catch (SQLException e) {
			if (callback != null)
				callback.callSyncError(e);
			plugin.getLogger().severe(String.format("Failed to remove co-owner(s) from database (#%d).", ownable.getID()));
			plugin.debugf("Failed to remove co-owner(s) from database (#%d). PreparedStatement failed.", ownable.getID());
			plugin.debug(e);
		}
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
	public void getBanksAndAccounts(final boolean showConsoleMessages, final Callback<Map<Bank, Set<Account>>> callback) {
		Utils.bukkitRunnable(() -> {
			try (Connection con = dataSource.getConnection();
				 ResultSet rs = con.createStatement().executeQuery("SELECT * FROM " + tableBanks)) {

				ValueGrabber values = new ValueGrabber(rs);

				Map<Bank, Set<Account>> banksAndAccounts = new HashMap<>();

				while (values.next()) {
					int bankID = values.getNextInt();
					plugin.debugf("Getting bank from database... (#%d)", bankID);
					String name = values.getNextString();
					String ownerUUID = values.getNextString();
					OfflinePlayer owner = ownerUUID != null ? Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)) : null;

					boolean countInterestDelayOffline = values.getNextBoolean();
					boolean reimburseAccountCreation = values.getNextBoolean();
					boolean payOnLowBalance = values.getNextBoolean();
					double interestRate = values.getNextDouble();
					double accountCreationPrice = values.getNextDouble();
					double minimumBalance = values.getNextDouble();
					double lowBalanceFee = values.getNextDouble();
					int initialInterestDelay = values.getNextInt();
					int allowedOfflinePayouts = values.getNextInt();
					int allowedOfflinePayoutsBeforeMultiplierReset = values.getNextInt();
					int offlineMultiplierDecrement = values.getNextInt();
					int withdrawalMultiplierDecrement = values.getNextInt();
					int playerBankAccountLimit = values.getNextInt();
					List<Integer> multipliers;
					try {
						multipliers = Arrays.stream(values.getNextString().split(", ")).map(Integer::parseInt).collect(Collectors.toList());
					} catch (NumberFormatException e) {
						multipliers = Config.multipliers.getDefault();
					}
					List<LocalTime> interestPayoutTimes;
					try {
						interestPayoutTimes = Arrays.stream(values.getNextString().split(", ")).map(LocalTime::parse).collect(Collectors.toList());
					} catch (DateTimeParseException e) {
						interestPayoutTimes = Config.interestPayoutTimes.getDefault();
					}
					BankConfig bankConfig = new BankConfig(countInterestDelayOffline, reimburseAccountCreation, payOnLowBalance,
							interestRate, accountCreationPrice, minimumBalance, lowBalanceFee, initialInterestDelay,
							allowedOfflinePayouts, allowedOfflinePayoutsBeforeMultiplierReset, offlineMultiplierDecrement,
							withdrawalMultiplierDecrement, playerBankAccountLimit, multipliers, interestPayoutTimes);

					String worldName = values.getNextString();
					World world = Bukkit.getWorld(worldName);
					if (world == null) {
						WorldNotFoundException e = new WorldNotFoundException(worldName);
						if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
							plugin.getLogger().warning(e.getMessage());
							notFoundWorlds.add(worldName);
						}
						plugin.debugf("Failed to get bank (#%d)",  bankID);
						plugin.debug(e);
						continue;
					}

					int minX = values.getNextInt();
					int maxX = values.getNextInt();
					int minY = values.getNextInt();
					int maxY = values.getNextInt();
					int minZ = values.getNextInt();
					int maxZ = values.getNextInt();
					String vertices = values.getNextString();
					Selection selection;
					if (vertices == null) {
						selection = CuboidSelection.of(world, new BlockVector3D(minX, minY, minZ), new BlockVector3D(maxX, maxY, maxZ));
					} else {
						selection = PolygonalSelection.of(world,
								Arrays.stream(vertices.substring(1, vertices.length() - 1).split("\\), \\(")).map(string -> {
									String[] xAndZ = string.split(", ");
									return new BlockVector2D(Integer.parseInt(xAndZ[0]), Integer.parseInt(xAndZ[1]));
								}).collect(Collectors.toList()),
								minY, maxY);
					}

					Set<OfflinePlayer> coowners = new HashSet<>();
					try (Statement s = con.createStatement();
						 ResultSet rs2 = s.executeQuery("SELECT CoOwnerUUID FROM " + tableCoOwnsBank + " WHERE BankID = " + bankID)) {

						while (rs2.next())
							coowners.add(Utils.getPlayerFromUUID(rs2.getString(1)));

					} catch (SQLException e) {
						plugin.getLogger().severe(String.format("Failed to get bank co-owners from database (#%d)", bankID));
						plugin.debugf("Failed to get bank co-owners database (#%d)", bankID);
						plugin.debug(e);
					}

					plugin.debugf("Initializing bank \"%s\"... (#%d)", ChatColor.stripColor(name), bankID);

					Bank bank = Bank.recreate(bankID, name, owner, coowners, selection, bankConfig);

					getAccountsAtBank(bank, showConsoleMessages, Callback.of(plugin,
							result -> banksAndAccounts.put(bank, result),
							callback::callSyncError
					));
				}

				if (callback != null)
					callback.callSyncResult(Collections.unmodifiableMap(banksAndAccounts));

			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to get banks from database.");
				plugin.debug("Failed to get banks from database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Gets all accounts registered at a certain bank from the database
	 *
	 * @param bank                The bank to get the accounts of
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all accounts (as
	 *                            {@code Collection<Account>})
	 */
	private void getAccountsAtBank(final Bank bank, final boolean showConsoleMessages, final Callback<Set<Account>> callback) {
		try (Connection con = dataSource.getConnection(); ResultSet rs = con.createStatement()
				.executeQuery("SELECT * FROM " + tableAccounts + " WHERE BankID = " + bank.getID())) {

			Set<Account> accounts = new HashSet<>();

			ValueGrabber values = new ValueGrabber(rs);

			while (values.next()) {

				int accountID = values.getNextInt();
				values.getNextInt();
				String nickname = values.getNextString();
				String ownerUUID = values.getNextString();
				OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));

				BigDecimal balance = BigDecimal.valueOf(values.getNextDouble());
				BigDecimal prevBalance = BigDecimal.valueOf(values.getNextDouble());

				int multiplierStage = values.getNextInt();
				int delayUntilNextPayout = values.getNextInt();
				int remainingOfflinePayouts = values.getNextInt();
				int remainingOfflinePayoutsUntilReset = values.getNextInt();

				String worldName = values.getNextString();
				World world = Bukkit.getWorld(worldName);
				if (world == null) {
					WorldNotFoundException e = new WorldNotFoundException(worldName);
					if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
						plugin.getLogger().warning(e.getMessage());
						notFoundWorlds.add(worldName);
					}
					plugin.debugf ("Failed to get account (#%d)", accountID);
					plugin.debug(e);
					continue;
				}
				int x = values.getNextInt();
				int y = values.getNextInt();
				int z = values.getNextInt();
				Location location = new Location(world, x, y, z);

				Set<OfflinePlayer> coowners = new HashSet<>();
				try (ResultSet rs2 = con.createStatement()
						.executeQuery("SELECT CoOwnerUUID FROM " + tableCoOwnsAccount + " WHERE AccountID = " + accountID)) {

					while (rs2.next())
						coowners.add(Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))));

				} catch (SQLException e) {
					plugin.getLogger().severe(String.format("Failed to get account co-owners from database (#%d)", accountID));
					plugin.debugf("Failed to get account co-owners database (#%d)", accountID);
					plugin.debug(e);
				}

				plugin.debugf("Initializing account #%d at bank \"%s\"", accountID, bank.getName());

				Account account = Account.reopen(accountID, owner, coowners, bank, location, nickname, balance, prevBalance,
						multiplierStage, delayUntilNextPayout, remainingOfflinePayouts, remainingOfflinePayoutsUntilReset);
				accounts.add(account);
			}

			if (callback != null)
				callback.callSyncResult(Collections.unmodifiableSet(accounts));

		} catch (SQLException e) {
			if (callback != null)
				callback.callSyncError(e);
			plugin.getLogger().severe(String.format("Failed to get accounts from database (bank #%d).", bank.getID()));
			plugin.debugf("Failed to get accounts from database (bank #%d).", bank.getID());
			plugin.debug(e);
		}
	}

	/**
	 * Log an account transaction to the database
	 *
	 * @param executor Player who performed a transaction
	 * @param account  The {@link Account} the player performed the transaction on
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountTransaction(final Player executor, final Account account, final BigDecimal amount, final Callback<Integer> callback) {
		if (!Config.enableAccountTransactionLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}
		Utils.bukkitRunnable(() -> {
			final String query = "INSERT INTO " + tableAccountTransactions +
					"(AccountID, ExecutorUUID, Amount, NewBalance, Timestamp, Time) " +
					"VALUES(?,?,?,?,?,?)";
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				long millis = System.currentTimeMillis();
				values.addNext(account.getID());
				values.addNext(executor.getUniqueId().toString());
				values.addNext(amount.doubleValue());
				values.addNext(account.getBalance().doubleValue());
				values.addNext(dateFormat.format(millis));
				values.addNext(millis);

				ps.executeUpdate();

				int transactionID = -1;
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					transactionID = rs.getInt(1);
				rs.close();
				if (callback != null)
					callback.callSyncResult(transactionID);

				plugin.debugf("Logged account transaction to database (#%d).", transactionID);
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to log account transaction to database.");
				plugin.debug("Failed to log account transaction to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Log an interest payout to the database
	 *
	 * @param account  The {@link Account} the interest was derived from
	 * @param amount   The {@link BigDecimal} final transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountInterest(final Account account, final BigDecimal amount, final Callback<Integer> callback) {
		if (!Config.enableAccountInterestLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}
		Utils.bukkitRunnable(() -> {
			final String query = "INSERT INTO " + tableAccountInterest +
					"(AccountID, BankID, Amount, Timestamp, Time) " +
					"VALUES(?,?,?,?,?)";
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				long millis = System.currentTimeMillis();
				values.addNext(account.getID());
				values.addNext(account.getBank().getID());
				values.addNext(amount.doubleValue());
				values.addNext(dateFormat.format(millis));
				values.addNext(millis);

				ps.executeUpdate();

				int interestID = -1;
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					interestID = rs.getInt(1);
				rs.close();
				if (callback != null)
					callback.callSyncResult(interestID);

				plugin.debugf("Logged interest to database (#%d at account #%d).", interestID, account.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to log interest to database.");
				plugin.debug("Failed to log interest to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	public void logBankRevenue(final Bank bank, final BigDecimal amount, final Callback<Integer> callback) {
		if (!Config.enableBankRevenueLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}
		Utils.bukkitRunnable(() -> {
			final String query = "INSERT INTO " + tableBankRevenue +
					"(BankID, Amount, Timestamp, Time) " +
					"VALUES(?,?,?,?)";
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				long millis = System.currentTimeMillis();
				values.addNext(bank.getID());
				values.addNext(amount.doubleValue());
				values.addNext(dateFormat.format(millis));
				values.addNext(millis);

				ps.executeUpdate();

				int revenueID = -1;
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					revenueID = rs.getInt(1);
				rs.close();
				if (callback != null)
					callback.callSyncResult(revenueID);

				plugin.debugf("Logged revenue to database (#%d at bank #%s).", revenueID, bank.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to log revenue to database.");
				plugin.debug("Failed to log revenue to database.");
				plugin.debug(e);
			}
		}).runTaskAsynchronously(plugin);
	}

	public void logLowBalanceFee(final Account account, final BigDecimal amount, final Callback<Integer> callback) {
		if (!Config.enableLowBalanceFeeLog) {
			if (callback != null)
				callback.callSyncResult(null);
			return;
		}
		Utils.bukkitRunnable(() -> {
			final String query = "INSERT INTO " + tableLowBalanceFees +
					"(AccountID, BankID, Amount, Timestamp, Time) " +
					"VALUES(?,?,?,?)";
			try (Connection con = dataSource.getConnection();
				 PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ValueInserter values = new ValueInserter(ps);

				long millis = System.currentTimeMillis();
				values.addNext(account.getID());
				values.addNext(account.getBank().getID());
				values.addNext(amount.doubleValue());
				values.addNext(dateFormat.format(millis));
				values.addNext(millis);

				ps.executeUpdate();

				int revenueID = -1;
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					revenueID = rs.getInt(1);
				rs.close();
				if (callback != null)
					callback.callSyncResult(revenueID);

				plugin.debugf("Logged low balance fee to database (#%d at account #%s).", revenueID, account.getID());
			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
				plugin.getLogger().severe("Failed to log low balance fee to database.");
				plugin.debug("Failed to log low balance fee to database.");
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
		if (Config.cleanupLogDays < 0)
			return;
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			long time = System.currentTimeMillis() - Config.cleanupLogDays * 86400000L;

			try (Statement s = dataSource.getConnection().createStatement()) {

				s.addBatch("DELETE FROM " + tableAccountTransactions + " WHERE Time < " + time);
				s.addBatch("DELETE FROM " + tableAccountInterest + " WHERE Time < " + time);
				s.addBatch("DELETE FROM " + tableBankRevenue + " WHERE Time < " + time);
				s.addBatch("DELETE FROM " + tableLowBalanceFees + " WHERE Time < " + time);
				s.addBatch("DELETE FROM " + tablePlayers + " WHERE LastSeen < " + time);

				int[] affectedRows = s.executeBatch();

				plugin.getLogger().info("Cleaned up banking logs.");
				plugin.debugf("Cleaned up banking logs (%d transactions, %d interests, %d revenues, %d low balance fees, %d players).",
						affectedRows[0], affectedRows[1], affectedRows[2], affectedRows[3], affectedRows[4]);
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
     * Logs player's last seen time to the database
     *
     * @param player    Player who logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLastSeen(final Player player, final Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			try (PreparedStatement ps = dataSource.getConnection()
					.prepareStatement("REPLACE INTO " + tablePlayers + " (PlayerUUID,Name,LastSeen) VALUES(?,?,?)")) {

				ps.setString(1, player.getUniqueId().toString());
				ps.setString(2, player.getName());
				ps.setLong(3, System.currentTimeMillis());
				ps.executeUpdate();

				if (callback != null)
					callback.callSyncResult(null);

				if (!player.isOnline())
					plugin.debug("Logged last seen time to database");

			} catch (final SQLException ex) {
				if (callback != null)
					callback.callSyncError(ex);
				plugin.getLogger().severe("Failed to log last seen time to database.");
				plugin.debug("Failed to log last seen time to database.");
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
	public void getAccountInterestEarnedOffline(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getOfflineRevenue(player, logoutTime, tableAccountInterest, callback);
    }

	/**
	 * Gets the revenue a player earned in bank profit while they were offline
	 *
	 * @param player     Player whose revenue to get
	 * @param logoutTime Time in milliseconds when he logged out the last time
	 * @param callback   Callback that - if succeeded - returns the revenue the
	 *                   player made while offline (as {@code double})
	 */
	public void getBankRevenueEarnedOffline(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getOfflineRevenue(player, logoutTime, tableBankRevenue, callback);
	}

    private void getOfflineRevenue(Player player, long logoutTime, String table, Callback<BigDecimal> callback) {
		Utils.bukkitRunnable(() -> {
			try (ResultSet rs = dataSource.getConnection().createStatement()
					.executeQuery("SELECT * FROM " + table + " WHERE PlayerUUID = '" + player.getUniqueId().toString() + "'")) {

				BigDecimal total = BigDecimal.ZERO;
				while (rs.next()) {
					if (rs.getLong("Time") > logoutTime) {
						total = total.add(BigDecimal.valueOf(Double.parseDouble(rs.getString("Amount"))));
					}
				}
				if (callback != null)
					callback.callSyncResult(total.setScale(2, RoundingMode.HALF_EVEN));

			} catch (SQLException ex) {
				if (callback != null)
					callback.callSyncError(ex);
				plugin.getLogger().severe("Failed to get earnings since last logout from database.");
				plugin.debug("Failed to get earnings from player \"" + player.getUniqueId().toString() + "\".");
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
			try (ResultSet rs = dataSource.getConnection().createStatement()
					.executeQuery("SELECT * FROM " + tablePlayers + " WHERE PlayerUUID = '" + player.getUniqueId().toString() + "'")) {

				long lastLogout = -1L;

				if (rs.next())
					lastLogout = rs.getLong(3);

				if (callback != null)
					callback.callSyncResult(lastLogout);

			} catch (SQLException e) {
				if (callback != null)
					callback.callSyncError(e);
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

	/**
	 * Helper class to insert values into a {@link PreparedStatement} in sequential order.
	 */
	private static class ValueInserter {

		private int index = 1;
		private final PreparedStatement ps;

		private ValueInserter(PreparedStatement ps) {
			this.ps = ps;
		}

		void addNext(String next) throws SQLException {
			ps.setString(index++, next);
		}

		void addNext(int next) throws SQLException {
			ps.setInt(index++, next);
		}

		void addNext(long next) throws SQLException {
			ps.setLong(index++, next);
		}

		void addNext(double next) throws SQLException {
			ps.setDouble(index++, next);
		}

		void addNext(boolean next) throws SQLException {
			ps.setString(index++, Boolean.toString(next));
		}

		<T> void addNext(Collection<T> next) throws SQLException {
			if (next != null)
				ps.setString(index++, next.stream().map(Objects::toString).collect(Collectors.joining(", ")));
			else
				ps.setString(index++, null);
		}

	}

	/**
	 * Helper class to extract values from a {@link ResultSet} in sequential order.
	 */
	private static class ValueGrabber {

		private int index = 1;
		private final ResultSet rs;

		private ValueGrabber(ResultSet rs) {
			this.rs = rs;
		}

		boolean next() throws SQLException {
			index = 1;
			return rs.next();
		}

		String getNextString() throws SQLException {
			return rs.getString(index++);
		}

		int getNextInt() throws SQLException {
			return rs.getInt(index++);
		}

		long getNextLong() throws SQLException {
			return rs.getLong(index++);
		}

		double getNextDouble() throws SQLException {
			return rs.getDouble(index++);
		}

		boolean getNextBoolean() throws SQLException {
			return Boolean.parseBoolean(getNextString());
		}

		void skip() {
			index++;
		}

	}

}
