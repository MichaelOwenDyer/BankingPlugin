package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Database {

	final BankingPlugin plugin = BankingPlugin.getInstance();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final Set<String> unknownWorldNames = new HashSet<>();
	private final int DATABASE_VERSION = 1;

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

	SqlErrorHandler forwardError(Callback<?> callback) {
		return (e, msg) -> {
			Callback.error(callback, e);
			throw e;
		};
	}

	final AfterQueryListener queryLogger = execution -> {
		if(execution.success()) {
			plugin.debugf(
					"Query took %s ms to execute: '%s'",
					execution.executionTimeMs(),
					execution.sql()
			);
		} else
			plugin.debug(execution.sqlException().orElseThrow(IllegalStateException::new));
	};

	final SqlErrorHandler handler = (e, msg) -> {
		plugin.debugf("Encountered a database error while executing query '%s'", msg.orElse("null"));
		plugin.debug(e);
		throw e;
	};

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

	/**
	 * Carry out any potential updates on the database structure if necessary.
	 * @return whether the database was updated or not
	 */
	private boolean update() {
		int version = getDatabaseVersion();
		if (version == DATABASE_VERSION)
			return false;
		Runnable[] updates = new Runnable[] { () -> {} }; // Updates can be added here in the future
		while (version < DATABASE_VERSION && version++ < updates.length)
			updates[version].run();
		setDatabaseVersion(DATABASE_VERSION);
		return true;
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

		async(() -> {
			disconnect();

			FluentJdbc fluentJdbc;
			try {
				dataSource = getDataSource();
				fluentJdbc = new FluentJdbcBuilder()
						.connectionProvider(dataSource)
						.afterQueryListener(queryLogger)
						.defaultSqlHandler(() -> handler)
						.defaultTransactionIsolation(Transaction.Isolation.SERIALIZABLE)
						.build();
			} catch (RuntimeException e) {
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			if (dataSource == null || fluentJdbc == null) {
				IllegalStateException e = new IllegalStateException("Data source / fluentJdbc is null");
				callback.onError(e);
				plugin.debug(e);
				return;
			}

			query = fluentJdbc.query();

			if (update())
				plugin.getLogger().info("Updating database finished.");

			plugin.debug("Starting table creation.");

			long createdTables = Stream.of(
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
					.map(UpdateQuery::run)
					.mapToLong(UpdateResult::affectedRows)
					.sum();

			plugin.debugf("Created %d missing tables.", createdTables);

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
		});
	}

	private HikariDataSource dataSource;
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
	 * Adds an account to the database.
	 *
	 * @param account  Account to add
	 * @param callback Callback that returns the new account ID
	 */
	public void addAccount(Account account, Callback<Integer> callback) {
		async(() -> {
			plugin.debugf("Adding account to the database.");
			final String replaceQuery = "REPLACE INTO " + tableAccounts + "(" + (account.hasID() ? "AccountID, " : "") +
					"BankID, Nickname, OwnerUUID, Balance, PreviousBalance, MultiplierStage, DelayUntilNextPayout, " +
					"RemainingOfflinePayouts, RemainingOfflinePayoutsUntilReset, World, Y, X1, Z1, X2, Z2) " +
					"VALUES(" + (account.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			final LinkedList<Object> params = getAttributes(account);
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

			plugin.debugf("Added account #%d to the database.", account.getID());
			Callback.yield(callback, account.getID());
		});
	}

	public void updateAccount(Account account, EnumSet<AccountField> fields, Callback<Void> callback) {
		async(() -> {
			String attributes = fields.stream()
					.map(a -> a.getName() + " = ?")
					.collect(Collectors.joining(", "));
			List<Object> params = fields.stream()
					.map(a -> a.getFrom(account))
					.collect(Collectors.toList());
			query.update("UPDATE " + tableAccounts + " " +
					"SET " + attributes + " " +
					"WHERE AccountID = ?")
					.params(params)
					.params(account.getID())
					.errorHandler(forwardError(callback))
					.run();
			Callback.yield(callback);
		});
	}

	/**
	 * Removes an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(Account account, Callback<Void> callback) {
		async(() -> {
			plugin.debugf("Removing account #%d from the database.", account.getID());

			long removedCoowners = query.transaction().in(() -> {
				query
						.update("DELETE FROM " + tableAccounts + " WHERE AccountID = ?")
						.params(account.getID())
						.errorHandler(forwardError(callback))
						.run();
				return query
						.update("DELETE FROM " + tableCoOwnsAccount + " WHERE AccountID = ?")
						.params(account.getID())
						.errorHandler(forwardError(callback))
						.run()
						.affectedRows();
			});

			plugin.debugf("Removed account #%d and %d coowners from the database.", account.getID(), removedCoowners);
			Callback.yield(callback);
		});
	}

	/**
	 * Adds a bank to the database
	 *
	 * @param bank     Bank to add
	 * @param callback Callback that - if succeeded - returns the ID the bank was
	 *                 given (as {@code int})
	 */
	public void addBank(Bank bank, Callback<Integer> callback) {
		async(() -> {
			plugin.debugf("Adding bank to the database.", bank.getID());
			final String replaceQuery = "REPLACE INTO " + tableBanks + "(" + (bank.hasID() ? "BankID, " : "") +
					"Name, OwnerUUID, CountInterestDelayOffline, ReimburseAccountCreation, PayOnLowBalance, " +
					"InterestRate, AccountCreationPrice, MinimumBalance, LowBalanceFee, InitialInterestDelay, " +
					"AllowedOfflinePayouts, AllowedOfflinePayoutsBeforeMultiplierReset, OfflineMultiplierDecrement, " +
					"WithdrawalMultiplierDecrement, PlayerBankAccountLimit, Multipliers, InterestPayoutTimes, " +
					"World, MinX, MaxX, MinY, MaxY, MinZ, MaxZ, PolygonVertices) " +
					"VALUES(" + (bank.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			final LinkedList<Object> params = getAttributes(bank);
			if (!bank.hasID())
				params.removeFirst();

			int id = query
					.update(replaceQuery)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(rs -> rs.getInt(1))
					.firstKey()
					.orElse(-1);

			bank.setID(id);

			plugin.debugf("Added bank #%d to the database.", bank.getID());
			Callback.yield(callback, bank.getID());
		});
	}

	/**
	 * Removes a bank from the database. All accounts associated with this bank will automatically be removed from the database as well.
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(Bank bank, Callback<Void> callback) {
		async(() -> {
			plugin.debugf("Removing bank #%d from the database.", bank.getID());
			long removedCoowners = query.transaction().in(
					() -> {
						query
								.update("DELETE FROM " + tableBanks + " WHERE BankID = ?")
								.params(bank.getID())
								.errorHandler(forwardError(callback))
								.run();
						return query
								.update("DELETE FROM " + tableCoOwnsBank + " WHERE BankID = ?")
								.params(bank.getID())
								.errorHandler(forwardError(callback))
								.run()
								.affectedRows();
					}
			);

			plugin.debugf("Removed bank #%d and %d coowners from the database.", bank.getID(), removedCoowners);
			Callback.yield(callback);
		});
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
		async(() -> {
			Map<Bank, Set<Account>> banksAndAccounts = getBanks(showConsoleMessages, callback).stream()
					.collect(Collectors.toMap(
							Function.identity(),
							bank -> getAccounts(bank, showConsoleMessages, callback)
					));
			Callback.yield(callback, Collections.unmodifiableMap(banksAndAccounts));
		});
	}

	/**
	 * Gets all banks from the database.
	 *
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 */
	private Set<Bank> getBanks(boolean showConsoleMessages, Callback<?> callback) {
		plugin.debug("Fetching banks from the database.");
		Set<Bank> banks = query
						.select("SELECT * FROM " + tableBanks)
						.errorHandler(forwardError(callback))
						.setResult(reconstructBank(showConsoleMessages));
		plugin.debugf("Found %d bank%s.", banks.size(), banks.size() == 1 ? "" : "s");
		return Collections.unmodifiableSet(banks);
	}

	/**
	 * Gets all accounts registered at a certain bank from the database.
	 *
	 * @param bank                The bank to get the accounts of
	 * @param showConsoleMessages Whether console messages (errors or warnings)
	 *                            should be shown
	 */
	private Set<Account> getAccounts(Bank bank, boolean showConsoleMessages, Callback<?> callback) {
		plugin.debugf("Fetching accounts at bank #%d from the database.", bank.getID());
		Set<Account> accounts = query
						.select("SELECT * FROM " + tableAccounts + " WHERE BankID = ?")
						.params(bank.getID())
						.errorHandler(forwardError(callback))
						.setResult(reconstructAccount(bank, showConsoleMessages));
		plugin.debugf("Found %d account%s.", accounts.size(), accounts.size() == 1 ? "" : "s");
		return Collections.unmodifiableSet(accounts);
	}

	private Mapper<Bank> reconstructBank(boolean showConsoleMessages) {
		return rs -> {
			ValueGrabber values = new ValueGrabber(rs);

			int bankID = values.getNextInt();
			plugin.debugf("Fetching bank #%d from the database.", bankID);

			String name = values.getNextString();
			String ownerUUID = values.getNextString();
			OfflinePlayer owner = Utils.nonNull(ownerUUID, id -> Bukkit.getOfflinePlayer(UUID.fromString(id)), () -> null);

			Boolean countInterestDelayOffline = values.getNextBooleanNullable();
			Boolean reimburseAccountCreation = values.getNextBooleanNullable();
			Boolean payOnLowBalance = values.getNextBooleanNullable();
			Double interestRate = values.getNextDoubleNullable();
			Double accountCreationPrice = values.getNextDoubleNullable();
			Double minimumBalance = values.getNextDoubleNullable();
			Double lowBalanceFee = values.getNextDoubleNullable();
			Integer initialInterestDelay = values.getNextInteger();
			Integer allowedOfflinePayouts = values.getNextInteger();
			Integer allowedOfflinePayoutsBeforeMultiplierReset = values.getNextInteger();
			Integer offlineMultiplierDecrement = values.getNextInteger();
			Integer withdrawalMultiplierDecrement = values.getNextInteger();
			Integer playerBankAccountLimit = values.getNextInteger();
			List<Integer> multipliers;
			try {
				multipliers = Arrays.stream(values.getNextString().split("\\s*,\\s*")).map(Integer::parseInt).collect(Collectors.toList());
			} catch (NumberFormatException e) {
				multipliers = Config.multipliers.getDefault();
			}
			List<LocalTime> interestPayoutTimes;
			try {
				interestPayoutTimes = Arrays.stream(values.getNextString().split("\\s*,\\s*")).map(LocalTime::parse).collect(Collectors.toList());
			} catch (DateTimeParseException e) {
				interestPayoutTimes = Config.interestPayoutTimes.getDefault();
			}

			String worldName = values.getNextString();
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				WorldNotFoundException e = new WorldNotFoundException(worldName);
				if (showConsoleMessages && !unknownWorldNames.contains(worldName)) {
					plugin.getLogger().warning(e.getMessage());
					unknownWorldNames.add(worldName);
				}
				plugin.debugf("Failed to identify world '%s' while fetching bank #%d from the database.", worldName, bankID);
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
			Selection selection;
			if (vertices != null) {
				List<BlockVector2D> points = Arrays.stream(vertices.substring(1, vertices.length() - 1).split("\\), \\(")).map(string -> {
					String[] xAndZ = string.split("\\s*,\\s");
					return new BlockVector2D(Integer.parseInt(xAndZ[0]), Integer.parseInt(xAndZ[1]));
				}).collect(Collectors.toList());
				selection = PolygonalSelection.of(world, points, minY, maxY);
			} else {
				selection = CuboidSelection.of(world, new BlockVector3D(minX, minY, minZ), new BlockVector3D(maxX, maxY, maxZ));
			}

			Set<OfflinePlayer> coowners = query
					.select("SELECT CoOwnerUUID FROM " + tableCoOwnsBank + " WHERE BankID = ?")
					.params(bankID)
					.setResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))));

			plugin.debugf("Found %d bank coowner%s.", coowners.size(), coowners.size() == 1 ? "" : "s");

			plugin.debugf("Initializing bank #%d (\"%s\")...", bankID, ChatColor.stripColor(name));
			return Bank.recreate(
					bankID,
					name,
					owner,
					coowners,
					selection,
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
		};
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
				if (showConsoleMessages && !unknownWorldNames.contains(worldName)) {
					plugin.getLogger().warning(e.getMessage());
					unknownWorldNames.add(worldName);
				}
				plugin.debugf("Failed to identify world '%s' while fetching account #%d from the database.", worldName, accountID);
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

			Set<OfflinePlayer> coowners = query
					.select("SELECT CoOwnerUUID FROM " + tableCoOwnsAccount + " WHERE AccountID = ?")
					.params(accountID)
					.setResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))));
			plugin.debugf("Found %d account coowner%s.", coowners.size(), coowners.size() == 1 ? "" : "s");

			plugin.debugf("Initializing account #%d at bank #%d (\"%s\")", accountID, bank.getID(), bank.getName());
			return Account.reopen(accountID, owner, coowners, bank, chestLocation, nickname, balance, prevBalance,
					multiplierStage, delayUntilNextPayout, remainingOfflinePayouts, remainingOfflinePayoutsUntilReset);
		};
	}

	/**
	 * Adds a bank co-owner to the database.
	 * @param bank bank the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		plugin.debugf("Adding co-owner %s to bank #%d.", coowner.getName(), bank.getID());
		addCoOwner(tableCoOwnsBank, "BankID", coowner.getUniqueId(), bank.getID(), callback, async);
	}


	/**
	 * Adds an account co-owner to the database.
	 * @param account account the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		plugin.debugf("Adding co-owner %s to account #%d.", coowner.getName(), account.getID());
		addCoOwner(tableCoOwnsAccount, "AccountID", coowner.getUniqueId(), account.getID(), callback, async);
	}

	private void addCoOwner(String table, String idAttribute, UUID coownerUUID, int entityID, Callback<Void> callback, boolean async) {
		run(() -> {
			query
					.update("REPLACE INTO " + table + "(CoOwnerUUID, " + idAttribute + ") VALUES(?,?)")
					.params(coownerUUID, entityID)
					.errorHandler(forwardError(callback))
					.run();
			Callback.yield(callback);
		}, async);
	}

	/**
	 * Removes a bank co-owner from the database.
	 * @param bank bank the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 * @param async whether to run this method asynchronously
	 */
	public void removeCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		plugin.debugf("Removing co-owner %s from bank #%d.", coowner.getName(), bank.getID());
		removeCoOwner(tableCoOwnsBank, "BankID", bank.getID(), coowner.getUniqueId(), callback, async);
	}

	/**
	 * Removes an account co-owner from the database.
	 * @param account account the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 * @param async whether to run this method asynchronously
	 */
	public void removeCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback, boolean async) {
		plugin.debugf("Removing co-owner %s from account #%d.", coowner.getName(), account.getID());
		removeCoOwner(tableCoOwnsAccount, "AccountID", account.getID(), coowner.getUniqueId(), callback, async);
	}

	private void removeCoOwner(String table, String idAttribute, int entityID, UUID coownerUUID, Callback<Void> callback, boolean async) {
		run(() -> {
			long affectedRows = query
					.update("DELETE FROM " + table + " WHERE " + idAttribute + " = ? AND CoOwnerUUID = ?")
					.params(entityID, coownerUUID)
					.errorHandler(forwardError(callback))
					.run()
					.affectedRows();
			if (affectedRows == 0)
				plugin.debugf("Found no co-owner to remove.");
			Callback.yield(callback);
		}, async);
	}

	/**
	 * Logs an account transaction to the database.
	 *
	 * @param executor Player who performed a transaction
	 * @param account  The {@link Account} the player performed the transaction on
	 * @param amount   The {@link BigDecimal} transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountTransaction(Player executor, Account account, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableAccountTransactions +
				" (AccountID, ExecutorUUID, Amount, NewBalance, Timestamp, Time) VALUES(?,?,?,?,?,?)";
		long millis = System.currentTimeMillis();
		final List<Object> params = Arrays.asList(
				account.getID(),
				executor.getUniqueId(),
				amount,
				account.getBalance(),
				dateFormat.format(millis),
				millis
		);
		log(Config.enableAccountTransactionLog, query, params, callback);
	}

	/**
	 * Logs an interest payout to the database.
	 *
	 * @param account  The {@link Account} the interest was derived from
	 * @param amount   The {@link BigDecimal} final transaction amount
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void logAccountInterest(Account account, BigDecimal amount, Callback<Integer> callback) {
		final String query = "INSERT INTO " + tableAccountInterest +
				" (AccountID, BankID, Amount, Timestamp, Time) VALUES(?,?,?,?,?)";
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
		final String query = "INSERT INTO " + tableBankRevenue + " (BankID, Amount, Timestamp, Time) VALUES(?,?,?,?)";
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
				" (AccountID, BankID, Amount, Timestamp, Time) VALUES(?,?,?,?)";
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
	 * Logs a player's last seen time to the database.
	 *
	 * @param player    Player who logged out
	 * @param callback  Callback that - if succeeded - returns {@code null}
	 */
	public void logLastSeen(Player player, Callback<Integer> callback) {
		final String query = "REPLACE INTO " + tablePlayers + " (PlayerUUID,Name,LastSeen) VALUES(?,?,?)";
		final List<Object> params = Arrays.asList(
				player.getUniqueId(),
				player.getName(),
				System.currentTimeMillis()
		);
		log(true, query, params, callback);
	}

	private void log(boolean configEnabled, String queryString, List<Object> params, Callback<Integer> callback) {
		if (!configEnabled)
			return;
		async(() -> {
			int id = query
					.update(queryString)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(rs -> rs.getInt(1))
					.firstKey().orElse(-1);
			Callback.yield(callback, id);
		});
	}

	/**
	 * Cleans up the economy log to reduce file size
	 */
	public void cleanUpLogs() {
		if (Config.cleanupLogDays < 0)
			return;

		run(() -> {
			final long time = System.currentTimeMillis() - Config.cleanupLogDays * 86400000L;

			long transactions = query.update("DELETE FROM " + tableAccountTransactions +
					" WHERE Time < " + time).run().affectedRows();
			long interest = query.update("DELETE FROM " + tableAccountInterest +
					" WHERE Time < " + time).run().affectedRows();
			long revenue = query.update("DELETE FROM " + tableBankRevenue +
					" WHERE Time < " + time).run().affectedRows();
			long lowBalanceFees = query.update("DELETE FROM " + tableLowBalanceFees +
					" WHERE Time < " + time).run().affectedRows();
			long players = query.update("DELETE FROM " + tablePlayers +
					" WHERE LastSeen < " + time).run().affectedRows();

			plugin.getLogger().info("Cleaned up banking logs.");
			plugin.debugf("Cleaned up banking logs (%d transactions, %d interests, %d revenues, %d low balance fees, %d players).",
					transactions, interest, revenue, lowBalanceFees, players);
		}, false); // TODO: Make async?
	}

	/**
	 * Gets the total interest a player has earned on their accounts between a particular time and now
	 *
	 * @param player     Player whose account interest is to be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the player's total account interest between then and now
	 */
	public void getTotalInterestEarnedSince(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		String timeFormatted = dateFormat.format(time);
		async(() -> {
			plugin.debugf("Fetching account interest for %s since last logout at %s.", playerName, timeFormatted);
			BigDecimal interest = query
					.select("SELECT SUM(Amount) " +
							"FROM " + tableAccountInterest + " INNER JOIN " + tableAccounts + " USING(AccountID) " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::new)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in account interest for %s.", Utils.format(interest), playerName);
			Callback.yield(callback, Utils.scale(interest));
		});
	}

	/**
	 * Gets the total low balance fees a player has paid on their accounts between a particular time and now
	 *
	 * @param player     Player whose low balance fees are to be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the total low balance fees paid by the player between then and now
	 */
	public void getTotalLowBalanceFeesPaidSince(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		String timeFormatted = dateFormat.format(time);
		async(() -> {
			plugin.debugf("Fetching low balance fees paid by %s since last logout at %s.", playerName, timeFormatted);
			BigDecimal fees = query
					.select("SELECT SUM(Amount) " +
							"FROM " + tableLowBalanceFees + " INNER JOIN " + tableAccounts + " USING(AccountID) " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::new)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in low balance fees paid by %s.", Utils.format(fees), playerName);
			Callback.yield(callback, Utils.scale(fees));
		});
	}

	/**
	 * Gets the bank profit a player earned between a particular time and now
	 *
	 * @param player     Player whose bank profits should be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the total bank profit between then and now
	 */
	public void getTotalBankProfitSinceLogout(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		String timeFormatted = dateFormat.format(time);
		async(() -> {
			plugin.debugf("Fetching bank revenue earned by %s since last logout at %s.", playerName, timeFormatted);
			BigDecimal revenue = query
					.select("SELECT SUM(Amount) " +
							"FROM " + tableBankRevenue + " NATURAL JOIN " + tableBanks + " " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::new)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in bank revenue earned by %s.", Utils.format(revenue), playerName);
			plugin.debugf("Fetching low balance fees received by %s since last logout at %s.", playerName, timeFormatted);
			BigDecimal fees = query
					.select("SELECT SUM(Amount) " +
							"FROM " + tableLowBalanceFees + " NATURAL JOIN " + tableBanks + " " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::new)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in low balance fees received by %s.", Utils.format(fees), playerName);
			plugin.debugf("Fetching bank interest paid by %s since last logout at %s.", playerName, timeFormatted);
			BigDecimal interest = query
					.select("SELECT SUM(Amount) " +
							"FROM " + tableAccountInterest + " NATURAL JOIN " + tableBanks + " " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::new)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in interest payments paid by %s.", Utils.format(interest), playerName);
			Callback.yield(callback, Utils.scale(revenue.add(fees).subtract(interest)));
		});
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
		String playerName = player.getName();
		async(() -> {
			plugin.debugf("Fetching last logout for %s.", playerName);
			long lastLogout = query
					.select("SELECT LastSeen FROM " + tablePlayers + " WHERE PlayerUUID = ?")
					.params(player.getUniqueId())
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getLong(1))
					.orElse(-1L);
			plugin.debugf("Found last logout for %s at %d.", playerName, lastLogout);
			Callback.yield(callback, lastLogout);
		});
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

		Integer getNextInteger() throws SQLException {
			int result = getNextInt();
			if (result == 0 && rs.wasNull())
				return null;
			return result;
		}

		long getNextLong() throws SQLException {
			return rs.getLong(index++);
		}

		Long getNextLongNullable() throws SQLException {
			long result = getNextLong();
			if (result == 0 && rs.wasNull())
				return null;
			return result;
		}

		double getNextDouble() throws SQLException {
			return rs.getDouble(index++);
		}

		Double getNextDoubleNullable() throws SQLException {
			double result = getNextDouble();
			if (result == 0 && rs.wasNull())
				return null;
			return result;
		}

		boolean getNextBoolean() throws SQLException {
			return rs.getBoolean(index++);
		}

		Boolean getNextBooleanNullable() throws SQLException {
			boolean result = getNextBoolean();
			if (!result && rs.wasNull())
				return null;
			return result;
		}

		BigDecimal getNextBigDecimal() throws SQLException {
			return rs.getBigDecimal(index++);
		}

		void skip() {
			index++;
		}

	}

	private LinkedList<Object> getAttributes(Account account) {
		ChestLocation loc = account.getChestLocation();
		BlockVector3D v1 = loc.getMinimumBlock();
		BlockVector3D v2 = loc.getMaximumBlock();
		return new LinkedList<>(Arrays.asList(
				account.getID(),
				account.getBank().getID(),
				account.getRawName(),
				account.getOwner().getUniqueId(),
				account.getBalance(),
				account.getPrevBalance(),
				account.getMultiplierStage(),
				account.getDelayUntilNextPayout(),
				account.getRemainingOfflinePayouts(),
				account.getRemainingOfflinePayoutsUntilReset(),
				loc.getWorld().getName(),
				v1.getY(),
				v1.getX(),
				v1.getZ(),
				v2.getX(),
				v2.getZ()
		));
	}

	private LinkedList<Object> getAttributes(Bank bank) {
		Selection sel = bank.getSelection();
		return new LinkedList<>(Arrays.asList(
				bank.getID(),
				bank.getRawName(),
				bank.getOwnerUUID(),
				bank.getCountInterestDelayOffline().getNullable(),
				bank.getReimburseAccountCreation().getNullable(),
				bank.getPayOnLowBalance().getNullable(),
				bank.getInterestRate().getNullable(),
				bank.getAccountCreationPrice().getNullable(),
				bank.getMinimumBalance().getNullable(),
				bank.getLowBalanceFee().getNullable(),
				bank.getInitialInterestDelay().getNullable(),
				bank.getAllowedOfflinePayouts().getNullable(),
				bank.getAllowedOfflinePayoutsBeforeReset().getNullable(),
				bank.getOfflineMultiplierDecrement().getNullable(),
				bank.getWithdrawalMultiplierDecrement().getNullable(),
				bank.getPlayerBankAccountLimit().getNullable(),
				bank.getMultipliers().getNullable(),
				bank.getInterestPayoutTimes().getNullable(),
				sel.getWorld().getName(),
				sel.getMinX(),
				sel.getMaxX(),
				sel.getMinY(),
				sel.getMaxY(),
				sel.getMinZ(),
				sel.getMaxZ(),
				sel.isPolygonal() ? ((PolygonalSelection) bank.getSelection()).getVertices() : null
		));
	}

	private void async(Runnable runnable) {
		run(runnable, true);
	}

	private void run(Runnable runnable) {
		run(runnable, false);
	}

	private void run(Runnable runnable, boolean async) {
		if (async)
			Utils.bukkitRunnable(runnable).runTaskAsynchronously(plugin);
		else
			runnable.run();
	}

}
