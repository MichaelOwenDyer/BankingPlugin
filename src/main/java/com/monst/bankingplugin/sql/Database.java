package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankConfig;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.WorldNotFoundException;
import com.monst.bankingplugin.geo.BlockVector2D;
import com.monst.bankingplugin.geo.BlockVector3D;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.locations.DoubleChestLocation;
import com.monst.bankingplugin.geo.locations.SingleChestLocation;
import com.monst.bankingplugin.geo.selections.CuboidSelection;
import com.monst.bankingplugin.geo.selections.PolygonalSelection;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.codejargon.fluentjdbc.api.FluentJdbc;
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder;
import org.codejargon.fluentjdbc.api.query.*;
import org.codejargon.fluentjdbc.api.query.listen.AfterQueryListener;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Database {

	private final Set<String> notFoundWorlds = new HashSet<>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String tableBanks = "Banks";
	String tableCoOwnsBank = "co_owns_bank";
	String tableAccounts = "Accounts";
	String tableCoOwnsAccount = "co_owns_account";
	String tableAccountTransactions = "AccountTransactions";
	String tableAccountInterest = "AccountInterest";
	String tableBankRevenue = "BankRevenue";
	String tableLowBalanceFees = "LowBalanceFees";
	String tablePlayers = "Players";
	String tableFields = "Fields";

	final BankingPlugin plugin = BankingPlugin.getInstance();
	HikariDataSource dataSource;
	FluentJdbc fluentJdbc;
	Query query;

	abstract HikariDataSource getDataSource();

	abstract String getQueryCreateTable(String tableName, String... attributes);

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

	private int getDatabaseVersion() {
		return query
				.select("SELECT Value FROM " + tableFields + " WHERE Field = 'Version'")
				.firstResult(rs -> rs.getInt("Value"))
				.orElse(0);
	}

	private void setDatabaseVersion(int version) {
		query
				.update("REPLACE INTO " + tableFields + " VALUES ('Version', ?)")
				.params(version)
				.run();
	}

	SqlErrorHandler forwardError(Callback<?> callback) {
		return (e, msg) -> {
			Callback.error(callback, e);
			throw e;
		};
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
	public void connect(Callback<int[]> callback) {

		Utils.bukkitRunnable(() -> {
			disconnect();

			dataSource = getDataSource();

			AfterQueryListener queryLogger = execution -> {
				if(execution.success()) {
					plugin.debugf(
							"Query took %s ms to execute: '%s'",
							execution.executionTimeMs(),
							execution.sql()
					);
				} else
					plugin.debug(execution.sqlException().orElseThrow(IllegalStateException::new));
			};

			SqlErrorHandler handler = (e, msg) -> {
				plugin.debugf("Encountered a database error while executing query '%s'", msg.orElse("null"));
				plugin.debug(e);
				throw e;
			};

			try {
				fluentJdbc = new FluentJdbcBuilder()
						.connectionProvider(dataSource)
						.afterQueryListener(queryLogger)
						.defaultSqlHandler(() -> handler)
						.build();
				query = fluentJdbc.query();
			} catch (Exception e) {
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			if (dataSource == null || fluentJdbc == null) {
				IllegalStateException e = new IllegalStateException("Data source/fluent jdbc is null");
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			if (update())
				plugin.getLogger().info("Updating database finished.");

			plugin.debug("Starting table creation");

			Stream.of(
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
			)
					.map(query::update)
					.map(q -> q.errorHandler(forwardError(callback)))
					.forEach(UpdateQuery::run);

			// Clean up economy log
			if (Config.cleanupLogDays > 0)
				cleanUpLogs();

			int accounts = query
					.select("SELECT COUNT(AccountID) FROM " + tableAccounts)
					.firstResult(rs -> rs.getInt(1))
					.orElseThrow(IllegalStateException::new);

			int banks = query
					.select("SELECT COUNT(BankID) FROM " + tableBanks)
					.firstResult(rs -> rs.getInt(1))
					.orElseThrow(IllegalStateException::new);

			Callback.yield(callback, new int[] { banks, accounts });

		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Carry out any potential updates on the database structure if necessary.
	 * @return whether the database was updated or not
	 */
	private boolean update() {
		return false;
	}

	/**
	 * Adds an account to the database
	 *
	 * @param account  Account to add
	 * @param callback Callback that - if succeeded - returns the ID the account was
	 *                 given (as {@code int})
	 */
	public void addAccount(Account account, Callback<Integer> callback) {
		Utils.bukkitRunnable(() -> {

			final String replaceQuery = constructReplaceQuery(tableAccounts, account.hasID(), "AccountID", "BankID",
					"Nickname", "OwnerUUID", "Balance", "PreviousBalance", "MultiplierStage", "DelayUntilNextPayout",
					"RemainingOfflinePayouts", "RemainingOfflinePayoutsUntilReset", "World", "Y", "X1", "Z1", "X2", "Z2"
			);

			final LinkedList<Object> params = accountAttributes(account);
			if (!account.hasID())
				params.removeFirst();

			int id = query
					.update(replaceQuery)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(rs -> rs.getInt(1))
					.firstKey().orElse(-1);

			account.setID(id);
			account.getBank().addAccount(account);

			if(!account.getCoOwners().isEmpty()) {
				Stream<List<?>> coOwnerEntries = account.getCoOwners().stream()
						.map(OfflinePlayer::getUniqueId)
						.map(uuid -> Arrays.asList(uuid, account.getID()));
				query
						.batch("INSERT INTO " + tableCoOwnsAccount + "(CoOwnerUUID, AccountID) VALUES(?,?)")
						.params(coOwnerEntries)
						.errorHandler(forwardError(callback))
						.run();
			}

			Callback.yield(callback, account.getID());
			plugin.debugf("Added account to database (#%d).", account.getID());
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Removes an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(Account account, Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			query.transaction().inNoResult(() -> {
				query
						.update("DELETE FROM " + tableAccounts + " WHERE AccountID = ?")
						.params(account.getID())
						.errorHandler(forwardError(callback))
						.run();
				query
						.update("DELETE FROM " + tableCoOwnsAccount + " WHERE AccountID = ?")
						.params(account.getID())
						.errorHandler(forwardError(callback))
						.run();
			});

			Callback.yield(callback);
			plugin.debugf("Removing account from database (#%d)", account.getID());
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Adds a bank to the database
	 *
	 * @param bank     Bank to add
	 * @param callback Callback that - if succeeded - returns the ID the bank was
	 *                 given (as {@code int})
	 */
	public void addBank(Bank bank, Callback<Integer> callback) {
		Utils.bukkitRunnable(() -> {

			final String replaceQuery = constructReplaceQuery(tableBanks, bank.hasID(), "BankID", "Name", "OwnerUUID",
					"CountInterestDelayOffline", "ReimburseAccountCreation", "PayOnLowBalance", "InterestRate",
					"AccountCreationPrice", "MinimumBalance", "LowBalanceFee", "InitialInterestDelay",
					"AllowedOfflinePayouts", "AllowedOfflinePayoutsBeforeMultiplierReset", "OfflineMultiplierDecrement",
					"WithdrawalMultiplierDecrement", "PlayerBankAccountLimit", "Multipliers", "InterestPayoutTimes",
					"World", "MinX", "MaxX", "MinY", "MaxY", "MinZ", "MaxZ", "PolygonVertices");

			final LinkedList<Object> params = bankAttributes(bank);
			if (!bank.hasID())
				params.removeFirst();

			int id = query
					.update(replaceQuery).params(params).errorHandler(forwardError(callback))
					.runFetchGenKeys(rs -> rs.getInt(1)).firstKey().orElse(-1);

			bank.setID(id);

			if(!bank.getCoOwners().isEmpty()) {
				Stream<List<?>> coOwnerEntries = bank.getCoOwners().stream()
						.map(OfflinePlayer::getUniqueId)
						.map(uuid -> Arrays.asList(uuid, bank.getID()));
				query
						.batch("INSERT INTO " + tableCoOwnsBank + "(CoOwnerUUID, BankID) VALUES(?,?)")
						.params(coOwnerEntries)
						.errorHandler(forwardError(callback))
						.run();
			}

			Callback.yield(callback, bank.getID());
			plugin.debugf("Adding bank to database (#%d)", bank.getID());
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Removes a bank from the database. All accounts associated with this bank will automatically be removed from the database as well.
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(Bank bank, Callback<Void> callback) {
		Utils.bukkitRunnable(() -> {
			query.transaction().inNoResult(
					() -> {
						query
								.update("DELETE FROM " + tableBanks + " WHERE BankID = ?")
								.params(bank.getID())
								.errorHandler(forwardError(callback))
								.run();
						query
								.update("DELETE FROM " + tableCoOwnsBank + " WHERE BankID = ?")
								.params(bank.getID())
								.errorHandler(forwardError(callback))
								.run();
					}
			);

			Callback.yield(callback);
			plugin.debugf("Removing bank from database (#%d)", bank.getID());
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Gets all banks and accounts from the database
	 *
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all banks and accounts (as
	 *                            {@code Map<Bank, Set<Account>>})
	 */
	public void getBanksAndAccounts(boolean showConsoleMessages, Callback<Map<Bank, Set<Account>>> callback) {
		Utils.bukkitRunnable(() -> {
			Map<Bank, Set<Account>> banksAndAccounts = new HashMap<>();
			getBanks(showConsoleMessages, Callback.of(plugin, banks ->
					banks.forEach(bank -> getBankAccounts(bank, showConsoleMessages, Callback.of(plugin,
							accounts -> banksAndAccounts.put(bank, accounts)
					)))
			));
			Callback.yield(callback, Collections.unmodifiableMap(banksAndAccounts));
		}).runTaskAsynchronously(plugin);
	}

	private void getBanks(boolean showConsoleMessages, Callback<Set<Bank>> callback) {
		Set<Bank> banks = new HashSet<>(
				query
						.select("SELECT * FROM " + tableBanks)
						.errorHandler(forwardError(callback))
						.listResult(reconstructBank(showConsoleMessages))
		);
		Callback.yield(callback, Collections.unmodifiableSet(banks));
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
	private void getBankAccounts(Bank bank, boolean showConsoleMessages, Callback<Set<Account>> callback) {
		Set<Account> accounts = new HashSet<>(
				query
						.select("SELECT * FROM " + tableAccounts + " WHERE BankID = ?")
						.params(bank.getID())
						.errorHandler(forwardError(callback))
						.listResult(reconstructAccount(bank, showConsoleMessages))
		);
		Callback.yield(callback, Collections.unmodifiableSet(accounts));
	}

	/**
	 * Adds a bank co-owner to the database.
	 * @param bank bank the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			query
					.update("REPLACE INTO " + tableCoOwnsBank + " (CoOwnerUUID, BankID) VALUES(?,?)")
					.params(coowner.getUniqueId().toString(), bank.getID())
					.errorHandler(forwardError(callback))
					.run();

			Callback.yield(callback);
		});
		if (async)
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.runTask(plugin);
	}


	/**
	 * Adds an account co-owner to the database.
	 * @param account account the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			query
					.update("REPLACE INTO " + tableCoOwnsAccount + " (CoOwnerUUID, AccountID) VALUES(?,?)")
					.params(coowner.getUniqueId().toString(), account.getID())
					.errorHandler(forwardError(callback))
					.run();

			Callback.yield(callback);
		});
		if (async)
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.runTask(plugin);
	}

	/**
	 * Removes a bank co-owner from the database.
	 * @param bank bank the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 * @param async whether to run this method asynchronously
	 */
	public void removeCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			UpdateResult result = query
					.update("DELETE FROM " + tableCoOwnsBank + " WHERE BankID = ? AND CoOwnerUUID = ?")
					.params(bank.getID(), coowner.getUniqueId())
					.errorHandler(forwardError(callback))
					.run();

			Callback.yield(callback);

			if (result.affectedRows() != 0)
				plugin.debugf("Removed co-owner from database (#%d).", bank.getID());
		});
		if (async)
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.runTask(plugin);
	}

	/**
	 * Removes an account co-owner from the database.
	 * @param account account the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 * @param async whether to run this method asynchronously
	 */
	public void removeCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
			UpdateResult result = query
					.update("DELETE FROM " + tableCoOwnsAccount + " WHERE AccountID = ? AND CoOwnerUUID = ?")
					.params(account.getID(), coowner.getUniqueId())
					.errorHandler(forwardError(callback))
					.run();

			Callback.yield(callback);

			if (result.affectedRows() != 0)
				plugin.debugf("Removed co-owner from database (#%d).", account.getID());
		});
		if (async)
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.runTask(plugin);
	}

	public void log(boolean configEnabled, String queryString, List<Object> params, Callback<Integer> callback) {
		if (!configEnabled) {
			Callback.yield(callback);
			return;
		}
		Utils.bukkitRunnable(() -> {
			int transactionID = query
					.update(queryString)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(rs -> rs.getInt(1))
					.firstKey().orElse(-1);
			Callback.yield(callback, transactionID);
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Log an account transaction to the database
	 *
	 * @param executor Player who performed a transaction
	 * @param account  The {@link Account} the player performed the transaction on
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountTransaction(Player executor, Account account, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableAccountTransactions +
				" (AccountID, ExecutorUUID, Amount, NewBalance, Timestamp, Time) " +
				"VALUES(?,?,?,?,?,?)";
		long millis = System.currentTimeMillis();
		final List<Object> params = Arrays.asList(
				account.getID(),
				executor.getUniqueId().toString(),
				amount,
				account.getBalance(),
				dateFormat.format(millis),
				millis
		);
		log(Config.enableAccountTransactionLog, query, params, callback);
	}

	/**
	 * Log an interest payout to the database
	 *
	 * @param account  The {@link Account} the interest was derived from
	 * @param amount   The {@link BigDecimal} final transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountInterest(Account account, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableAccountInterest +
				"(AccountID, BankID, Amount, Timestamp, Time) " +
				"VALUES(?,?,?,?,?)";
		long millis = System.currentTimeMillis();
		final List<Object> params = Arrays.asList(
				account.getID(),
				account.getBank().getID(),
				amount,
				dateFormat.format(millis),
				millis
		);
		log(Config.enableAccountInterestLog, query, params, callback);
	}

	public void logBankRevenue(Bank bank, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableBankRevenue +
				"(BankID, Amount, Timestamp, Time) " +
				"VALUES(?,?,?,?)";
		long millis = System.currentTimeMillis();
		List<Object> params = Arrays.asList(
				bank.getID(),
				amount,
				dateFormat.format(millis),
				millis
		);
		log(Config.enableBankRevenueLog, query, params, callback);
	}

	public void logLowBalanceFee(Account account, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableLowBalanceFees +
				"(AccountID, BankID, Amount, Timestamp, Time) " +
				"VALUES(?,?,?,?)";
		long millis = System.currentTimeMillis();
		List<Object> params = Arrays.asList(
				account.getID(),
				account.getBank().getID(),
				amount,
				dateFormat.format(millis),
				millis
		);
		log(Config.enableLowBalanceFeeLog, query, params, callback);
	}

	/**
	 * Cleans up the economy log to reduce file size
	 */
	public void cleanUpLogs() {
		if (Config.cleanupLogDays < 0)
			return;
		Utils.bukkitRunnable(() -> {

			final long time = System.currentTimeMillis() - Config.cleanupLogDays * 86400000L;

			long transactions = query.update("DELETE FROM " + tableAccountTransactions + " WHERE Time < " + time).run().affectedRows();
			long interest = query.update("DELETE FROM " + tableAccountInterest + " WHERE Time < " + time).run().affectedRows();
			long revenue = query.update("DELETE FROM " + tableBankRevenue + " WHERE Time < " + time).run().affectedRows();
			long lowBalanceFees = query.update("DELETE FROM " + tableLowBalanceFees + " WHERE Time < " + time).run().affectedRows();
			long players = query.update("DELETE FROM " + tablePlayers + " WHERE LastSeen < " + time).run().affectedRows();

			plugin.getLogger().info("Cleaned up banking logs.");
			plugin.debugf("Cleaned up banking logs (%d transactions, %d interests, %d revenues, %d low balance fees, %d players).",
					transactions, interest, revenue, lowBalanceFees, players);
		}).runTaskAsynchronously(plugin);
	}

	/**
	 * Logs player's last seen time to the database
	 *
	 * @param player    Player who logged out
	 * @param callback  Callback that - if succeeded - returns {@code null}
	 */
	public void logLastSeen(Player player, Callback<Integer> callback) {
		final String query = "REPLACE INTO " + tablePlayers + " (PlayerUUID,Name,LastSeen) VALUES(?,?,?)";
		final List<Object> params = Arrays.asList(
				player.getUniqueId().toString(),
				player.getName(),
				System.currentTimeMillis()
		);
		log(true, query, params, callback);
	}

	/**
	 * Gets the revenue a player received in account interest while they were offline
	 *
	 * @param player     Player whose revenue to get
	 * @param logoutTime Time in milliseconds when he logged out the last time
	 * @param callback   Callback that - if succeeded - returns the revenue the
	 *                   player made while offline (as {@code double})
	 */
	public void getTotalAccountInterestSinceLogout(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getTotalSinceLogout(player, logoutTime, tableAccountInterest, callback);
	}

	/**
	 * Gets the revenue a player earned in bank profit while they were offline
	 *
	 * @param player     Player whose revenue to get
	 * @param logoutTime Time in milliseconds when he logged out the last time
	 * @param callback   Callback that - if succeeded - returns the revenue the
	 *                   player made while offline (as {@code double})
	 */
	public void getTotalBankRevenueSinceLogout(Player player, long logoutTime, Callback<BigDecimal> callback) {
		getTotalSinceLogout(player, logoutTime, tableBankRevenue, callback);
	}

	private void getTotalSinceLogout(Player player, long logoutTime, String table, Callback<BigDecimal> callback) {
		Utils.bukkitRunnable(() -> {
			BigDecimal sum = query
					.select("SELECT Amount FROM " + table + " WHERE PlayerUUID = ? AND Time > ?")
					.params(player.getUniqueId().toString(), logoutTime)
					.errorHandler(forwardError(callback))
					.listResult(rs -> rs.getLong(1))
					.stream()
					.map(BigDecimal::valueOf)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			Callback.yield(callback, Utils.scale(sum));
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
			long lastLogout = query
					.select("SELECT LastSeen FROM " + tablePlayers + " WHERE PlayerUUID = ?")
					.params(player.getUniqueId().toString())
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getLong(1))
					.orElse(-1L);
			Callback.yield(callback, lastLogout);
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

		BigDecimal getNextBigDecimal() throws SQLException {
			return rs.getBigDecimal(index++);
		}

		void skip() {
			index++;
		}

	}

	private Mapper<Account> reconstructAccount(Bank bank, boolean showConsoleMessages) {
		return rs -> {

			ValueGrabber values = new ValueGrabber(rs);

			int accountID = values.getNextInt();
			values.skip(); // Skip BankID
			String nickname = values.getNextString();
			OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(values.getNextString()));

			BigDecimal balance = values.getNextBigDecimal();
			BigDecimal prevBalance = values.getNextBigDecimal();

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
				plugin.debugf("Failed to get account (#%d)", accountID);
				plugin.debug(e);
				return null;
			}
			int y = values.getNextInt();
			int x1 = values.getNextInt();
			int z1 = values.getNextInt();
			int x2 = values.getNextInt();
			int z2 = values.getNextInt();
			BlockVector3D v1 = new BlockVector3D(x1, y, z1);
			ChestLocation chestLocation;
			if (x1 == x2 && z1 == z2)
				chestLocation = SingleChestLocation.from(world, v1);
			else
				chestLocation = DoubleChestLocation.from(world, v1, new BlockVector3D(x2, y, z2));

			Set<OfflinePlayer> coowners = new HashSet<>(
					query
							.select("SELECT CoOwnerUUID FROM " + tableCoOwnsAccount + " WHERE AccountID = ?")
							.params(accountID)
							.listResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))))
			);

			plugin.debugf("Initializing account #%d at bank \"%s\"", accountID, bank.getName());

			return Account.reopen(accountID, owner, coowners, bank, chestLocation, nickname, balance, prevBalance,
					multiplierStage, delayUntilNextPayout, remainingOfflinePayouts, remainingOfflinePayoutsUntilReset);
		};
	}

	private Mapper<Bank> reconstructBank(boolean showConsoleMessages) {
		return rs -> {
			ValueGrabber values = new ValueGrabber(rs);

			int bankID = values.getNextInt();
			plugin.debugf("Getting bank from database... (#%d)", bankID);

			String name = values.getNextString();
			String ownerUUID = values.getNextString();
			OfflinePlayer owner = Utils.nonNull(ownerUUID, id -> Bukkit.getOfflinePlayer(UUID.fromString(id)), () -> null);

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
			BankConfig bankConfig = new BankConfig(
					countInterestDelayOffline,
					reimburseAccountCreation,
					payOnLowBalance,
					interestRate,
					accountCreationPrice,
					minimumBalance,
					lowBalanceFee,
					initialInterestDelay,
					allowedOfflinePayouts,
					allowedOfflinePayoutsBeforeMultiplierReset,
					offlineMultiplierDecrement,
					withdrawalMultiplierDecrement,
					playerBankAccountLimit,
					multipliers,
					interestPayoutTimes
			);

			String worldName = values.getNextString();
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				WorldNotFoundException e = new WorldNotFoundException(worldName);
				if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
					plugin.getLogger().warning(e.getMessage());
					notFoundWorlds.add(worldName);
				}
				plugin.debugf("Failed to get bank (#%d)", bankID);
				plugin.debug(e);
				return null;
			}

			int minX = values.getNextInt();
			int maxX = values.getNextInt();
			int minY = values.getNextInt();
			int maxY = values.getNextInt();
			int minZ = values.getNextInt();
			int maxZ = values.getNextInt();
			String vertices = values.getNextString();
			Selection selection = Utils.nonNull(vertices, v -> {
						List<BlockVector2D> points = Arrays.stream(vertices.substring(1, vertices.length() - 1).split("\\), \\(")).map(string -> {
							String[] xAndZ = string.split(", ");
							return new BlockVector2D(Integer.parseInt(xAndZ[0]), Integer.parseInt(xAndZ[1]));
						}).collect(Collectors.toList());
						return PolygonalSelection.of(world, points, minY, maxY);
					}, () -> CuboidSelection.of(world, new BlockVector3D(minX, minY, minZ), new BlockVector3D(maxX, maxY, maxZ))
			);

			Set<OfflinePlayer> coowners = new HashSet<>(
					query
							.select("SELECT CoOwnerUUID FROM " + tableCoOwnsBank + " WHERE BankID = ?")
							.params(bankID)
							.listResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))))
			);

			plugin.debugf("Initializing bank \"%s\"... (#%d)", ChatColor.stripColor(name), bankID);

			return Bank.recreate(bankID, name, owner, coowners, selection, bankConfig);
		};
	}

	private LinkedList<Object> accountAttributes(Account account) {
		BlockVector3D v1 = account.getChestLocation().getMinimumBlock();
		BlockVector3D v2 = account.getChestLocation().getMaximumBlock();
		return new LinkedList<>(Arrays.asList(
				account.getID(),
				account.getBank().getID(),
				account.getRawName(),
				account.getOwner().getUniqueId().toString(),
				account.getBalance(),
				account.getPrevBalance(),
				account.getMultiplierStage(),
				account.getDelayUntilNextPayout(),
				account.getRemainingOfflinePayouts(),
				account.getRemainingOfflinePayoutsUntilReset(),
				account.getChestLocation().getWorld().getName(),
				v1.getY(),
				v1.getX(),
				v1.getZ(),
				v2.getX(),
				v2.getZ()
		));
	}

	private LinkedList<Object> bankAttributes(Bank bank) {
		return new LinkedList<>(Arrays.asList(
				bank.getID(),
				bank.getRawName(),
				bank.getOwnerUUID(),
				bank.get(BankField.COUNT_INTEREST_DELAY_OFFLINE),
				bank.get(BankField.REIMBURSE_ACCOUNT_CREATION),
				bank.get(BankField.PAY_ON_LOW_BALANCE),
				bank.get(BankField.INTEREST_RATE),
				bank.get(BankField.ACCOUNT_CREATION_PRICE),
				bank.get(BankField.MINIMUM_BALANCE),
				bank.get(BankField.LOW_BALANCE_FEE),
				bank.get(BankField.INITIAL_INTEREST_DELAY),
				bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS),
				bank.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET),
				bank.get(BankField.OFFLINE_MULTIPLIER_DECREMENT),
				bank.get(BankField.WITHDRAWAL_MULTIPLIER_DECREMENT),
				bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT),
				bank.get(BankField.MULTIPLIERS),
				bank.get(BankField.INTEREST_PAYOUT_TIMES),
				bank.getSelection().getWorld().getName(),
				bank.getSelection().getMinX(),
				bank.getSelection().getMaxX(),
				bank.getSelection().getMinY(),
				bank.getSelection().getMaxY(),
				bank.getSelection().getMinZ(),
				bank.getSelection().getMaxZ(),
				bank.getSelection().isPolygonal() ?
						((PolygonalSelection) bank.getSelection()).getVertices() : null
		));
	}

	private static String constructReplaceQuery(String tableName, boolean hasID, String... attributes) {
		StringBuilder sb = new StringBuilder();
		sb.append("REPLACE INTO ").append(tableName);
		LinkedList<String> list = new LinkedList<>(Arrays.asList(attributes));
		if (!hasID)
			list.removeFirst();
		sb.append(list.stream().collect(Collectors.joining(",", " (", ") ")));
		sb.append("VALUES");
		sb.append(list.stream().map(s -> "?").collect(Collectors.joining(",", "(", ")")));
		return sb.toString();
	}

}
