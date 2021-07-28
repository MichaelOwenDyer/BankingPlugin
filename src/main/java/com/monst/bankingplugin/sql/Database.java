package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.WorldNotFoundException;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.exceptions.parse.TimeParseException;
import com.monst.bankingplugin.geo.Vector2D;
import com.monst.bankingplugin.geo.Vector3D;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.geo.regions.CuboidBankRegion;
import com.monst.bankingplugin.geo.regions.PolygonalBankRegion;
import com.monst.bankingplugin.sql.logging.AccountInterest;
import com.monst.bankingplugin.sql.logging.AccountTransaction;
import com.monst.bankingplugin.sql.logging.BankIncome;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.codejargon.fluentjdbc.api.FluentJdbc;
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder;
import org.codejargon.fluentjdbc.api.ParamSetter;
import org.codejargon.fluentjdbc.api.mapper.Mappers;
import org.codejargon.fluentjdbc.api.mapper.ObjectMappers;
import org.codejargon.fluentjdbc.api.query.*;
import org.codejargon.fluentjdbc.api.query.listen.AfterQueryListener;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Database {

	// Disable log messages from Hikari
	static {
		Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.OFF);
	}

	final BankingPlugin plugin;
	private final int DATABASE_VERSION = 1;
	private final Set<String> unknownWorldNames = new HashSet<>();
	private final ObjectMappers objectMappers = ObjectMappers.builder().build();

	final String tableBanks = "Banks";
	final String tableCoOwnsBank = "co_owns_bank";
	final String tableAccounts = "Accounts";
	final String tableCoOwnsAccount = "co_owns_account";
	final String tableAccountTransactions = "AccountTransactions";
	final String tableAccountInterest = "AccountInterest";
	final String tableBankIncome = "BankIncome";
	final String tablePlayers = "Players";
	final String tableFields = "Fields";

	Query query;

	public Database(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	abstract HikariDataSource getDataSource();

	abstract String getQueryCreateTable(String tableName, String... attributes);

	abstract String getQueryCreateTableBanks();

	abstract String getQueryCreateTableCoOwnsBank();

	abstract String getQueryCreateTableAccounts();

	abstract String getQueryCreateTableCoOwnsAccount();

	abstract String getQueryCreateTableAccountTransactions();

	abstract String getQueryCreateTableAccountInterest();

	abstract String getQueryCreateTableBankIncome();

	abstract String getQueryCreateTablePlayers();

	abstract String getQueryCreateTableFields();

	SqlErrorHandler forwardError(Callback<?> callback) {
		return (e, msg) -> {
			if (callback != null)
				callback.onError(e);
			throw e;
		};
	}

	private int getDatabaseVersion() {
		return query
				.select("SELECT Value FROM " + tableFields + " WHERE Field = 'Version'")
				.firstResult(rs -> rs.getInt("Value"))
				.orElse(0);
	}

	private void setDatabaseVersion() {
		query
				.update("REPLACE INTO " + tableFields + " VALUES ('Version', ?)")
				.params(DATABASE_VERSION)
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
		while (version < DATABASE_VERSION && version < updates.length) {
			updates[version].run();
			version++;
		}
		setDatabaseVersion();
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
		plugin.async(() -> {
			disconnect();

			ParamSetter<UUID> uuidParamSetter = (uuid, ps, i) -> ps.setString(i, uuid.toString());
			@SuppressWarnings("rawtypes")
			Map<Class, ParamSetter> paramSetters = new HashMap<>();
			paramSetters.put(UUID.class, uuidParamSetter);

			FluentJdbc fluentJdbc;
			SqlErrorHandler handler = (e, query) -> {
				plugin.debugf("Encountered a database error while executing query '%s'", query.orElse("null"));
				plugin.debug(e);
				throw e;
			};
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
			try {
				dataSource = getDataSource();
				fluentJdbc = new FluentJdbcBuilder()
						.connectionProvider(dataSource)
						.afterQueryListener(queryLogger)
						.defaultSqlHandler(() -> handler)
						.paramSetters(paramSetters)
						.defaultTransactionIsolation(Transaction.Isolation.SERIALIZABLE)
						.defaultFetchSize(0)
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

			plugin.debug("Starting table creation.");

			long createdTables = Stream.of(
					getQueryCreateTableBanks(), // Create banks table
					getQueryCreateTableCoOwnsBank(), // Create co_owns_bank table
					getQueryCreateTableAccounts(), // Create accounts table
					getQueryCreateTableCoOwnsAccount(), // Create co_owns_account table
					getQueryCreateTableAccountTransactions(), // Create account transaction log table
					getQueryCreateTableAccountInterest(), // Create account interest log table
					getQueryCreateTableBankIncome(), // Create bank income log table
					getQueryCreateTablePlayers(), // Create players table
					getQueryCreateTableFields() // Create fields table
			)
					.map(query::update)
					.map(q -> q.errorHandler(forwardError(callback)))
					.map(UpdateQuery::run)
					.mapToLong(UpdateResult::affectedRows)
					.sum();

			plugin.debugf("Created %d missing tables.", createdTables);

			if (update())
				plugin.getLogger().info("Updating database finished.");

			// Clean up economy log
			if (Config.cleanupLogDays.get() > 0)
				cleanUpLogs();

			int accounts = query
					.select("SELECT COUNT(AccountID) FROM " + tableAccounts)
					.firstResult(Mappers.singleInteger())
					.orElseThrow(IllegalStateException::new);
			int banks = query
					.select("SELECT COUNT(BankID) FROM " + tableBanks)
					.firstResult(Mappers.singleInteger())
					.orElseThrow(IllegalStateException::new);
			Callback.callSyncResult(callback, new int[] { banks, accounts });
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
		plugin.async(() -> {
			plugin.debugf("Adding account to the database.");
			final String replaceQuery = "REPLACE INTO " + tableAccounts + "(" + (account.hasID() ? "AccountID, " : "") +
					"BankID, Nickname, OwnerUUID, Balance, PreviousBalance, MultiplierStage, DelayUntilNextPayout, " +
					"RemainingOfflinePayouts, World, Y, X1, Z1, X2, Z2) " +
					"VALUES(" + (account.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			final LinkedList<Object> params = getAttributes(account);
			if (!account.hasID())
				params.removeFirst();

			int id = query
					.update(replaceQuery)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(Mappers.singleInteger())
					.firstKey().orElse(-1);

			account.setID(id);
			account.getBank().addAccount(account);

			plugin.debugf("Added account #%d to the database.", account.getID());
			Callback.callSyncResult(callback, account.getID());
		});
	}

	/**
	 * Updates an account in the database.
	 * @param account the account to update
	 * @param fields the attributes to update
	 * @param callback callback which return returns null on success
	 */
	public void updateAccount(Account account, EnumSet<AccountField> fields, Callback<Void> callback) {
		plugin.async(() -> {
			String attributes = fields.stream()
					.map(f -> f.getDatabaseAttribute() + " = ?")
					.collect(Collectors.joining(", "));
			List<Object> params = fields.stream()
					.map(f -> f.getFrom(account))
					.collect(Collectors.toList());
			query.update("UPDATE " + tableAccounts + " " +
					"SET " + attributes + " " +
					"WHERE AccountID = ?")
					.params(params)
					.params(account.getID())
					.errorHandler(forwardError(callback))
					.run();
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Removes an account from the database
	 *
	 * @param account  Account to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeAccount(Account account, Callback<Void> callback) {
		plugin.async(() -> {
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
			Callback.callSyncResult(callback);
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
		plugin.async(() -> {
			plugin.debugf("Adding bank to the database.", bank.getID());
			final String replaceQuery = "REPLACE INTO " + tableBanks + "(" + (bank.hasID() ? "BankID, " : "") +
					"Name, OwnerUUID, CountInterestDelayOffline, ReimburseAccountCreation, PayOnLowBalance, " +
					"InterestRate, AccountCreationPrice, MinimumBalance, LowBalanceFee, InitialInterestDelay, " +
					"AllowedOfflinePayouts, OfflineMultiplierDecrement, WithdrawalMultiplierDecrement, " +
					"PlayerBankAccountLimit, Multipliers, InterestPayoutTimes, " +
					"World, MinX, MaxX, MinY, MaxY, MinZ, MaxZ, PolygonVertices) " +
					"VALUES(" + (bank.hasID() ? "?," : "") + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			final LinkedList<Object> params = getAttributes(bank);
			if (!bank.hasID())
				params.removeFirst();

			int id = query
					.update(replaceQuery)
					.params(params)
					.errorHandler(forwardError(callback))
					.runFetchGenKeys(Mappers.singleInteger())
					.firstKey()
					.orElse(-1);

			bank.setID(id);

			plugin.debugf("Added bank #%d to the database.", bank.getID());
			Callback.callSyncResult(callback, bank.getID());
		});
	}

	/**
	 * Updates a bank in the database.
	 * @param bank the bank to update
	 * @param fields the attributes to update
	 * @param callback callback which return returns null on success
	 */
	public void updateBank(Bank bank, EnumSet<BankField> fields, Callback<Void> callback) {
		plugin.async(() -> {
			String attributes = fields.stream()
					.map(f -> f.getDatabaseAttribute() + " = ?")
					.collect(Collectors.joining(", "));
			List<Object> params = fields.stream()
					.map(f -> f.getFrom(bank))
					.collect(Collectors.toList());
			query.update("UPDATE " + tableBanks + " " +
					"SET " + attributes + " " +
					"WHERE BankID = ?")
					.params(params)
					.params(bank.getID())
					.errorHandler(forwardError(callback))
					.run();
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Removes a bank from the database. All accounts associated with this bank will automatically be removed from the database as well.
	 *
	 * @param bank     Bank to remove
	 * @param callback Callback that - if succeeded - returns {@code null}
	 */
	public void removeBank(Bank bank, Callback<Void> callback) {
		plugin.async(() -> {
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
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Gets all banks and accounts from the database
	 *
	 * @param callback            Callback that - if succeeded - returns a read-only
	 *                            collection of all banks and accounts (as
	 *                            {@code Map<Bank, Set<Account>>})
	 */
	public void getBanksAndAccounts(Callback<Map<Bank, Set<Account>>> callback) {
		plugin.async(() -> {
			Map<Bank, Set<Account>> banksAndAccounts = getBanks(callback).stream()
					.collect(Collectors.toMap(
							bank -> bank,
							bank -> getAccounts(bank, callback)
					));
			Callback.callSyncResult(callback, Collections.unmodifiableMap(banksAndAccounts));
		});
	}

	/**
	 * Gets all banks from the database.
	 *
	 */
	private Set<Bank> getBanks(Callback<?> callback) {
		plugin.debug("Fetching banks from the database.");
		Set<Bank> banks = query
						.select("SELECT * FROM " + tableBanks)
						.errorHandler(forwardError(callback))
						.setResult(reconstructBank());
		plugin.debugf("Found %d bank%s.", banks.size(), banks.size() == 1 ? "" : "s");
		return Collections.unmodifiableSet(banks);
	}

	/**
	 * Gets all accounts registered at a certain bank from the database.
	 *  @param bank                The bank to get the accounts of
	 *
	 */
	private Set<Account> getAccounts(Bank bank, Callback<?> callback) {
		plugin.debugf("Fetching accounts at bank #%d from the database.", bank.getID());
		Set<Account> accounts = query
						.select("SELECT * FROM " + tableAccounts + " WHERE BankID = ?")
						.params(bank.getID())
						.errorHandler(forwardError(callback))
						.setResult(reconstructAccount(bank));
		plugin.debugf("Found %d account%s.", accounts.size(), accounts.size() == 1 ? "" : "s");
		return Collections.unmodifiableSet(accounts);
	}

	private Mapper<Bank> reconstructBank() {
		return rs -> {
			ValueGrabber values = new ValueGrabber(rs);

			int bankID = values.getNextInt();
			plugin.debugf("Fetching bank #%d from the database.", bankID);

			String name = values.getNextString();
			String ownerUUID = values.getNextString();
			OfflinePlayer owner = Optional.ofNullable(ownerUUID).map(UUID::fromString).map(Bukkit::getOfflinePlayer).orElse(null);

			Boolean countInterestDelayOffline = values.getNextBooleanNullable();
			Boolean reimburseAccountCreation = values.getNextBooleanNullable();
			Boolean payOnLowBalance = values.getNextBooleanNullable();
			Double interestRate = values.getNextDoubleNullable();
			Double accountCreationPrice = values.getNextDoubleNullable();
			Double minimumBalance = values.getNextDoubleNullable();
			Double lowBalanceFee = values.getNextDoubleNullable();
			Integer initialInterestDelay = values.getNextInteger();
			Integer allowedOfflinePayouts = values.getNextInteger();
			Integer offlineMultiplierDecrement = values.getNextInteger();
			Integer withdrawalMultiplierDecrement = values.getNextInteger();
			Integer playerBankAccountLimit = values.getNextInteger();

			List<Integer> multipliers = new ArrayList<>();
			String multiplierList = Optional.ofNullable(values.getNextString()).map(s -> s.replaceAll("[\\[\\]]", "")).orElse("");
			for (String s : multiplierList.split("\\s*(,|\\s)\\s*"))
				try {
					multipliers.add(Config.multipliers.parseSingle(s));
				} catch (IntegerParseException ignored) {}
			if (multipliers.isEmpty())
				multipliers = Config.multipliers.getDefault();

			Set<LocalTime> interestPayoutTimes = new LinkedHashSet<>();
			String payoutTimesList = Optional.ofNullable(values.getNextString()).map(s -> s.replaceAll("[\\[\\]]", "")).orElse("");
			for (String s : payoutTimesList.split("\\s*(,|\\s)\\s*"))
				try {
					interestPayoutTimes.add(Config.interestPayoutTimes.parseSingle(s));
				} catch (TimeParseException ignored) {}

			String worldName = values.getNextString();
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				WorldNotFoundException e = new WorldNotFoundException(worldName);
				if (!unknownWorldNames.contains(worldName)) {
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
			BankRegion bankRegion;
			if (vertices != null) {
				Vector2D[] points =
						Arrays.stream(vertices.split("\\)\\s*[,.;:|]\\s*\\("))
						.map(Vector2D::parse)
						.toArray(Vector2D[]::new);
				bankRegion = PolygonalBankRegion.of(world, points, minY, maxY);
			} else
				bankRegion = CuboidBankRegion.of(world, new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));

			Set<OfflinePlayer> coowners = query
					.select("SELECT CoOwnerUUID FROM " + tableCoOwnsBank + " WHERE BankID = ?")
					.params(bankID)
					.setResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))));

			plugin.debugf("Found %d bank coowner%s.", coowners.size(), coowners.size() == 1 ? "" : "s");

			plugin.debugf("Initializing bank #%d (\"%s\")...", bankID, ChatColor.stripColor(name));
			return Bank.reopen(
					bankID,
					name,
					owner,
					coowners,
					bankRegion,
					countInterestDelayOffline,
					reimburseAccountCreation,
					payOnLowBalance,
					interestRate,
					accountCreationPrice,
					minimumBalance,
					lowBalanceFee,
					initialInterestDelay,
					allowedOfflinePayouts,
                    offlineMultiplierDecrement,
					withdrawalMultiplierDecrement,
					playerBankAccountLimit,
					multipliers,
					interestPayoutTimes
			);
		};
	}

	private Mapper<Account> reconstructAccount(Bank bank) {
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

			String worldName = values.getNextString();
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				WorldNotFoundException e = new WorldNotFoundException(worldName);
				if (!unknownWorldNames.contains(worldName)) {
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
			AccountLocation accountLocation = AccountLocation.at(world, y, x1, z1, x2, z2);

			Set<OfflinePlayer> coowners = query
					.select("SELECT CoOwnerUUID FROM " + tableCoOwnsAccount + " WHERE AccountID = ?")
					.params(accountID)
					.setResult(rs2 -> Bukkit.getOfflinePlayer(UUID.fromString(rs2.getString(1))));
			plugin.debugf("Found %d account coowner%s.", coowners.size(), coowners.size() == 1 ? "" : "s");

			plugin.debugf("Initializing account #%d at bank #%d (\"%s\")", accountID, bank.getID(), bank.getName());
			return Account.reopen(accountID, owner, coowners, bank, accountLocation, nickname, balance, prevBalance,
					multiplierStage, delayUntilNextPayout, remainingOfflinePayouts);
		};
	}

	/**
	 * Adds a bank co-owner to the database.
	 * @param bank bank the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback) {
		plugin.debugf("Adding co-owner %s to bank #%d.", coowner.getName(), bank.getID());
		plugin.async(() -> {
			query
					.update("REPLACE INTO " + tableCoOwnsBank + "(CoOwnerUUID, BankID) VALUES(?,?)")
					.params(coowner.getUniqueId(), bank.getID())
					.errorHandler(forwardError(callback))
					.run();
			Callback.callSyncResult(callback);
		});
	}


	/**
	 * Adds an account co-owner to the database.
	 * @param account account the player co-owns
	 * @param coowner the co-owner to be added
	 */
	public void addCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback) {
		plugin.debugf("Adding co-owner %s to account #%d.", coowner.getName(), account.getID());
		plugin.async(() -> {
			query
					.update("REPLACE INTO " + tableCoOwnsAccount + "(CoOwnerUUID, AccountID) VALUES(?,?)")
					.params(coowner.getUniqueId(), account.getID())
					.errorHandler(forwardError(callback))
					.run();
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Removes a bank co-owner from the database.
	 * @param bank bank the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 */
	public void removeCoOwner(Bank bank, OfflinePlayer coowner, Callback<Void> callback) {
		plugin.debugf("Removing co-owner %s from bank #%d.", coowner.getName(), bank.getID());
		plugin.async(() -> {
			long affectedRows = query
					.update("DELETE FROM " + tableCoOwnsBank + " WHERE BankID = ? AND CoOwnerUUID = ?")
					.params(bank.getID(), coowner.getUniqueId())
					.errorHandler(forwardError(callback))
					.run()
					.affectedRows();
			if (affectedRows == 0)
				plugin.debugf("Found no co-owner to remove.");
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Removes an account co-owner from the database.
	 * @param account account the player no longer co-owns
	 * @param coowner the co-owner to be removed
	 */
	public void removeCoOwner(Account account, OfflinePlayer coowner, Callback<Void> callback) {
		plugin.debugf("Removing co-owner %s from account #%d.", coowner.getName(), account.getID());
		plugin.async(() -> {
			long affectedRows = query
					.update("DELETE FROM " + tableCoOwnsAccount + " WHERE AccountID = ? AND CoOwnerUUID = ?")
					.params(account.getID(), coowner.getUniqueId())
					.errorHandler(forwardError(callback))
					.run()
					.affectedRows();
			if (affectedRows == 0)
				plugin.debugf("Found no co-owner to remove.");
			Callback.callSyncResult(callback);
		});
	}

	/**
	 * Logs an account transaction to the database.
	 *
	 * @param transaction The {@link AccountTransaction} to log
	 */
	public void logAccountTransaction(AccountTransaction transaction) {
		if (!Config.enableAccountTransactionLog.get())
			return;
		plugin.async(() -> {
			plugin.debugf("Logging account transaction of %s at account #%d.",
					Utils.format(transaction.getAmount()), transaction.getAccountID());
			List<Object> params = Arrays.asList(
					transaction.getAccountID(),
					transaction.getBankID(),
					transaction.getExecutorUUID(),
					transaction.getExecutorName(),
					transaction.getNewBalance(),
					transaction.getPreviousBalance(),
					transaction.getAmount(),
					Utils.formatTime(transaction.getTime()),
					transaction.getTime()
			);
			query
					.update("INSERT INTO " + tableAccountTransactions + " (AccountID, BankID, ExecutorUUID, " +
							"ExecutorName, NewBalance, PreviousBalance, Amount, Timestamp, Time) VALUES(?,?,?,?,?,?,?,?,?)")
					.params(params)
					.run();
		});
	}

	/**
	 * Logs an interest payout to the database.
	 *
	 * @param interest  The {@link AccountInterest} to log
	 */
	public void logAccountInterest(AccountInterest interest) {
		if (!Config.enableAccountInterestLog.get())
			return;
		plugin.async(() -> {
			plugin.debugf("Logging %s in interest, %s in fees to account #%d.",
					Utils.format(interest.getInterest()), Utils.format(interest.getLowBalanceFee()), interest.getAccountID());
			List<Object> params = Arrays.asList(
					interest.getAccountID(),
					interest.getBankID(),
					interest.getInterest(),
					interest.getLowBalanceFee(),
					interest.getFinalPayment(),
					Utils.formatTime(interest.getTime()),
					interest.getTime()
			);
			query
					.update("INSERT INTO " + tableAccountInterest +
							" (AccountID, BankID, Interest, LowBalanceFee, FinalPayment, Timestamp, Time) VALUES(?,?,?,?,?,?,?)")
					.params(params)
					.run();
		});
	}

	/**
	 * Logs bank income to the database.
	 *
	 * @param income The {@link BankIncome} to log
	 */
	public void logBankIncome(BankIncome income) {
		if (!Config.enableBankIncomeLog.get())
			return;
		plugin.async(() -> {
			plugin.debugf("Logging %s in revenue, %s in interest, %s in fees to bank #%d.",
					Utils.format(income.getRevenue()), Utils.format(income.getInterest()),
					Utils.format(income.getLowBalanceFees()), income.getBankID());
			List<Object> params = Arrays.asList(
					income.getBankID(),
					income.getRevenue(),
					income.getInterest(),
					income.getLowBalanceFees(),
					income.getProfit(),
					Utils.formatTime(income.getTime()),
					income.getTime()
			);
			query
					.update("INSERT INTO " + tableBankIncome + " " +
							"(BankID, Revenue, Interest, LowBalanceFees, Profit, Timestamp, Time) VALUES(?,?,?,?,?,?,?)")
					.params(params)
					.run();
		});
	}


	/**
	 * Logs a player's last seen time to the database.
	 *
	 * @param player    Player who logged out
	 */
	public void logLastSeen(Player player) {
		plugin.async(() -> {
			List<Object> params = Arrays.asList(
					player.getUniqueId(),
					player.getName(),
					System.currentTimeMillis()
			);
			query
					.update("REPLACE INTO " + tablePlayers + " (PlayerUUID, Name, LastSeen) VALUES(?,?,?)")
					.params(params)
					.run();
		});
	}

	public void logLastSeen(Collection<Player> players) {
		plugin.async(() -> {
			Stream<List<?>> params = players.stream()
					.map(p -> Arrays.asList(
							p.getUniqueId(),
							p.getName(),
							System.currentTimeMillis())
					);
			query
					.batch("REPLACE INTO " + tablePlayers + " (PlayerUUID, Name, LastSeen) VALUES (?,?,?)")
					.params(params)
					.run();
		});
	}

	/**
	 * Cleans up the log to reduce file size
	 */
	public void cleanUpLogs() {
		plugin.sync(() -> {
			final long time = System.currentTimeMillis() - Config.cleanupLogDays.get() * 86400000L;

			long transactions = query.update("DELETE FROM " + tableAccountTransactions +
					" WHERE Time < " + time).run().affectedRows();
			long interest = query.update("DELETE FROM " + tableAccountInterest +
					" WHERE Time < " + time).run().affectedRows();
			long revenue = query.update("DELETE FROM " + tableBankIncome +
					" WHERE Time < " + time).run().affectedRows();
			long players = query.update("DELETE FROM " + tablePlayers +
					" WHERE LastSeen < " + time).run().affectedRows();

			plugin.getLogger().info("Cleaned up banking logs.");
			plugin.debugf("Cleaned up banking logs (%d transactions, %d interests, %d revenues, %d players).",
					transactions, interest, revenue, players);
		}); // TODO: Make async?
	}

	final Mapper<AccountTransaction> transactionMapper = objectMappers.forClass(AccountTransaction.class);

	public void getTransactionsAtAccount(Account account, Callback<Collection<AccountTransaction>> callback) {
		plugin.async(() -> {
			plugin.debugf("Fetching transactions at account #%d.", account.getID());
			List<AccountTransaction> result = query
					.select("SELECT * " +
							"FROM " + tableAccountTransactions + " " +
							"WHERE AccountID = ? " +
							"ORDER BY TransactionID DESC")
					.params(account.getID())
					.errorHandler(forwardError(callback))
					.listResult(transactionMapper);
			plugin.debugf("Found %d transactions at account #%d.", result.size(), account.getID());
			Callback.callSyncResult(callback, result);
		});
	}

	public void getTransactionsAtBank(Bank bank, Callback<Collection<AccountTransaction>> callback) {
		plugin.async(() -> {
			plugin.debugf("Fetching transactions at bank #%d.", bank.getID());
			List<AccountTransaction> result = query
					.select("SELECT * " +
							"FROM " + tableAccountTransactions + " " +
							"WHERE BankID = ? " +
							"ORDER BY TransactionID DESC")
					.params(bank.getID())
					.errorHandler(forwardError(callback))
					.listResult(transactionMapper);
			plugin.debugf("Found %d transactions at bank #%d.", result.size(), bank.getID());
			Callback.callSyncResult(callback, result);
		});
	}

	final Mapper<AccountInterest> interestMapper = objectMappers.forClass(AccountInterest.class);

	public void getInterestPaymentsAtAccount(Account account, Callback<Collection<AccountInterest>> callback) {
		plugin.async(() -> {
			plugin.debugf("Fetching account interest payments at account #%d.", account.getID());
			List<AccountInterest> result = query
					.select("SELECT * " +
							"FROM " + tableAccountInterest + " " +
							"WHERE AccountID = ? " +
							"ORDER BY InterestID DESC")
					.params(account.getID())
					.errorHandler(forwardError(callback))
					.listResult(interestMapper);
			plugin.debugf("Found %d interest payments at account #%d.", result.size(), account.getID());
			Callback.callSyncResult(callback, result);
		});
	}

	public void getInterestPaymentsAtBank(Bank bank, Callback<Collection<AccountInterest>> callback) {
		plugin.async(() -> {
			plugin.debugf("Fetching account interest payments at bank #%d.", bank.getID());
			List<AccountInterest> result = query
					.select("SELECT * " +
							"FROM " + tableAccountInterest + " " +
							"WHERE BankID = ? " +
							"ORDER BY InterestID DESC")
					.params(bank.getID())
					.errorHandler(forwardError(callback))
					.listResult(interestMapper);
			plugin.debugf("Found %d interest payments at bank #%d.", result.size(), bank.getID());
			Callback.callSyncResult(callback, result);
		});
	}

	final Mapper<BankIncome> revenueMapper = objectMappers.forClass(BankIncome.class);

	public void getIncomesAtBank(Bank bank, Callback<Collection<BankIncome>> callback) {
		plugin.async(() -> {
			plugin.debugf("Fetching income at bank #%d.", bank.getID());
			List<BankIncome> result = query
					.select("SELECT * " +
							"FROM " + tableBankIncome + " " +
							"WHERE BankID = ? " +
							"ORDER BY IncomeID DESC")
					.params(bank.getID())
					.errorHandler(forwardError(callback))
					.listResult(revenueMapper);
			plugin.debugf("Found %d income entries at bank #%d.", result.size(), bank.getID());
			Callback.callSyncResult(callback, result);
		});
	}

	/**
	 * Gets the total interest a player has earned on their accounts between a particular time and now
	 *
	 * @param player     Player whose account interest is to be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the player's total account interest between then and now
	 */
	public void getInterestEarnedByPlayerSince(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		plugin.async(() -> {
			plugin.debugf("Fetching account interest for %s since last logout at %s.", playerName, Utils.formatTime(time));
			BigDecimal interest = query
					.select("SELECT SUM(FinalPayment) " +
							"FROM " + tableAccountInterest + " INNER JOIN " + tableAccounts + " USING(AccountID) " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::valueOf)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in account interest for %s.", Utils.format(interest), playerName);
			Callback.callSyncResult(callback, QuickMath.scale(interest));
		});
	}

	/**
	 * Gets the total low balance fees a player has paid on their accounts between a particular time and now
	 *
	 * @param player     Player whose low balance fees are to be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the total low balance fees paid by the player between then and now
	 */
	public void getLowBalanceFeesPaidByPlayerSince(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		plugin.async(() -> {
			plugin.debugf("Fetching low balance fees paid by %s since last logout at %s.", playerName, Utils.formatTime(time));
			BigDecimal fees = query
					.select("SELECT SUM(LowBalanceFee) " +
							"FROM " + tableAccountInterest + " INNER JOIN " + tableAccounts + " USING(AccountID) " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::valueOf)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in low balance fees paid by %s.", Utils.format(fees), playerName);
			Callback.callSyncResult(callback, QuickMath.scale(fees));
		});
	}

	/**
	 * Gets the bank profit a player earned between a particular time and now
	 *
	 * @param player     Player whose bank profits should be looked up
	 * @param time 		 Time in milliseconds
	 * @param callback   Callback that returns the total bank profit between then and now
	 */
	public void getBankProfitEarnedByPlayerSince(Player player, long time, Callback<BigDecimal> callback) {
		String playerName = player.getName();
		plugin.async(() -> {
			plugin.debugf("Fetching bank profit earned by %s since last logout at %s.", playerName, Utils.formatTime(time));
			BigDecimal revenue = query
					.select("SELECT SUM(Profit) " +
							"FROM " + tableBankIncome + " NATURAL JOIN " + tableBanks + " " +
							"WHERE OwnerUUID = ? AND Time > ?")
					.params(player.getUniqueId(), time)
					.errorHandler(forwardError(callback))
					.firstResult(rs -> rs.getDouble(1))
					.map(BigDecimal::valueOf)
					.orElse(BigDecimal.ZERO);
			plugin.debugf("Found %s in bank profit earned by %s.", Utils.format(revenue), playerName);
			Callback.callSyncResult(callback, QuickMath.scale(revenue));
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
		plugin.async(() -> {
			plugin.debugf("Fetching last logout for %s.", playerName);
			long lastLogout = query
					.select("SELECT LastSeen FROM " + tablePlayers + " WHERE PlayerUUID = ?")
					.params(player.getUniqueId())
					.errorHandler(forwardError(callback))
					.firstResult(Mappers.singleLong())
					.orElse(-1L);
			plugin.debugf("Found last logout for %s at %d.", playerName, lastLogout);
			Callback.callSyncResult(callback, lastLogout);
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

		String getNextString() throws SQLException {
			return rs.getString(index++);
		}

		int getNextInt() throws SQLException {
			return rs.getInt(index++);
		}

		Integer getNextInteger() throws SQLException {
			int result = getNextInt();
			return rs.wasNull() ? null : result;
		}

		double getNextDouble() throws SQLException {
			return rs.getDouble(index++);
		}

		Double getNextDoubleNullable() throws SQLException {
			double result = getNextDouble();
			return rs.wasNull() ? null : result;
		}

		boolean getNextBoolean() throws SQLException {
			return rs.getBoolean(index++);
		}

		Boolean getNextBooleanNullable() throws SQLException {
			boolean result = getNextBoolean();
			return rs.wasNull() ? null : result;
		}

		BigDecimal getNextBigDecimal() throws SQLException {
			return rs.getBigDecimal(index++);
		}

		void skip() {
			index++;
		}

	}

	private LinkedList<Object> getAttributes(Account account) {
		AccountLocation loc = account.getLocation();
		Block v1 = loc.getMinimumBlock();
		Block v2 = loc.getMaximumBlock();
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
				loc.getWorld().getName(),
				v1.getY(),
				v1.getX(),
				v1.getZ(),
				v2.getX(),
				v2.getZ()
		));
	}

	private LinkedList<Object> getAttributes(Bank bank) {
		BankRegion region = bank.getRegion();
		return new LinkedList<>(Arrays.asList(
				bank.getID(),
				bank.getRawName(),
				bank.getOwnerUUID(),
				bank.getCountInterestDelayOffline().getCustomValue(),
				bank.getReimburseAccountCreation().getCustomValue(),
				bank.getPayOnLowBalance().getCustomValue(),
				bank.getInterestRate().getCustomValue(),
				bank.getAccountCreationPrice().getCustomValue(),
				bank.getMinimumBalance().getCustomValue(),
				bank.getLowBalanceFee().getCustomValue(),
				bank.getInitialInterestDelay().getCustomValue(),
				bank.getAllowedOfflinePayouts().getCustomValue(),
				bank.getOfflineMultiplierDecrement().getCustomValue(),
				bank.getWithdrawalMultiplierDecrement().getCustomValue(),
				bank.getPlayerBankAccountLimit().getCustomValue(),
				bank.getMultipliers().getCustomValue(),
				bank.getInterestPayoutTimes().getCustomValue(),
				region.getWorld().getName(),
				region.getMinX(),
				region.getMaxX(),
				region.getMinY(),
				region.getMaxY(),
				region.getMinZ(),
				region.getMaxZ(),
				region.isCuboid() ? null : region.getVertices()
		));
	}

}
