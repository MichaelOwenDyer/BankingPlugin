package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.values.*;
import com.monst.bankingplugin.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Config {

	/**
	 * The account command of BankingPlugin <i>(default: account)</i>
	 **/
	public static AccountCommandName accountCommandName;

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
    public static BankCommandName bankCommandName;

    /**
     * The control command of BankingPlugin <i>(default: bp)</i>
     **/
    public static ControlCommandName controlCommandName;

    /**
     * The real-life times for account interest payouts.
     **/
	public static InterestPayoutTimes interestPayoutTimes;

    /**
     * The default baseline account interest rate.
     **/
	public static InterestRate interestRate;

    /**
     * The list of default interest multipliers in sequential order.
     **/
	public static Multipliers multipliers;

    /**
	 * The default number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static InitialInterestDelay initialInterestDelay;

	/**
	 * Whether to decrement the interest delay period while a player is offline.
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static CountInterestDelayOffline countInterestDelayOffline;

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static AllowedOfflinePayouts allowedOfflinePayouts;

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static OfflineMultiplierDecrement offlineMultiplierDecrement;

	/**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static WithdrawalMultiplierDecrement withdrawalMultiplierDecrement;

	/**
	 * The price a player has to pay in order to create an account.
	 **/
	public static AccountCreationPrice accountCreationPrice;

	/**
	 * Whether the account creation price should be refunded at removal.
	 */
	public static ReimburseAccountCreation reimburseAccountCreation;

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static BankCreationPrice bankCreationPrice;

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public static ReimburseBankCreation reimburseBankCreation;

	/**
	 * The default minimum balance.
	 */
	public static MinimumBalance minimumBalance;

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static LowBalanceFee lowBalanceFee;

	/**
	 * Whether or not accounts still earn interest on a balance lower than the minimum.
	 */
	public static PayOnLowBalance payOnLowBalance;

	/**
	 * The default account limit per player per bank.
	 */
	public static PlayerBankAccountLimit playerBankAccountLimit;

    /**
     * The default bank ownership limit for players whose limit is not set via a permission.
     **/
	public static DefaultBankLimit defaultBankLimit;

    /**
     * The default account ownership limit for players whose limit is not set via a permission.
     **/
	public static DefaultAccountLimit defaultAccountLimit;

	/**
	 * The minimum bank volume.
	 */
	public static MinimumBankVolume minimumBankVolume;

	/**
	 * The default bank volume limit for players whose limit is not set via a
	 * permission.
	 */
	public static MaximumBankVolume maximumBankVolume;

	/**
	 * Determines how default bank configuration values should behave on server default change.
	 */
	public static StubbornBanks stubbornBanks;

	/**
	 * Whether a bank owner should be allowed to create an account at their own
	 * bank.
	 */
	public static AllowSelfBanking allowSelfBanking;

	/**
	 * Whether remove requests should be confirmed.
	 */
	public static ConfirmOnRemove confirmOnRemove;

	/**
	 * Whether to require confirmation (second execution) of removeall commands on
	 * the server.
	 */
	public static ConfirmOnRemoveAll confirmOnRemoveAll;

	/**
	 * Whether account transfer requests should be confirmed.
	 */
	public static ConfirmOnTransfer confirmOnTransfer;

	/**
	 * Whether bank or account owners should be automatically added as co-owners
	 * when ownership is transferred to another player.
	 */
	public static TrustOnTransfer trustOnTransfer;

	/**
	 * The item with which a player can click an account chest to retrieve information.
	 **/
	public static AccountInfoItem accountInfoItem;

    /**
     * <p>Whether the update checker should run on start and notify players on join.</p>
     * The command is not affected by this setting and will continue to check for updates.
     **/
	public static EnableUpdateChecker enableUpdateChecker;

    /**
     * Whether account deposits and withdrawals should be logged in the database.
     **/
	public static EnableAccountTransactionLog enableAccountTransactionLog;

	/**
	 * Whether interest payouts should be logged in the database.
	 **/
	public static EnableAccountInterestLog enableAccountInterestLog;

	/**
	 * Whether bank income should be logged in the database.
	 */
	public static EnableBankIncomeLog enableBankIncomeLog;

	/**
	 * Whether the debug log file should be created.
	 **/
	public static EnableDebugLog enableDebugLog;

    /**
     * <p>Sets the time limit for cleaning up the deposit-withdrawal log in days</p>
     *
     * If this is equal to {@code 0}, the log will not be cleaned.
     **/
	public static CleanupLogDays cleanupLogDays;

	/**
	 * Whether WorldGuard integration should be enabled.
	 **/
	public static EnableWorldGuardIntegration enableWorldGuardIntegration;

    /**
     * Whether GriefPrevention integration should be enabled.
     **/
	public static EnableGriefPreventionIntegration enableGriefPreventionIntegration;

	/**
	 * Whether WorldEdit integration should be enabled.
	 **/
	public static EnableWorldEditIntegration enableWorldEditIntegration;

	/**
	 * The default value for the custom WorldGuard flag 'create-bank'
	 **/
	public static WorldGuardDefaultFlagValue worldGuardDefaultFlagValue;

    /**
	 * <p>
	 * Whether accounts should automatically be removed from the database if an
	 * error occurs while loading
	 * </p>
	 * (e.g. when no chest is found at an account's location)
	 */
	public static RemoveAccountOnError removeAccountOnError;

	/**
	 * <p>
	 * List containing items for which a player will earn no interest in their
	 * account.
	 * </p>
	 * If this list contains an item (e.g "STONE", "STONE:1"), it's in the
	 * blacklist.
	 **/
    public static Blacklist blacklist;

	/**
	 * The bank revenue function.
	 */
	public static BankRevenueFunction bankRevenueFunction;

	/**
	 * Worlds where banking should be disabled
	 */
	public static DisabledWorlds disabledWorlds;

	/**
	 * The regex pattern that bank names and account nicknames should be matched
	 * against.
	 */
	public static NameRegex nameRegex;

	/**
	 * Whether to enable the plugin startup message in the console upon server start.
	 */
	public static EnableStartupMessage enableStartupMessage;

	/**
	 * The language file to use.
	 */
	public static LanguageFile languageFile;

	/**
	 * The path to use for the database file.
	 */
	public static DatabaseFile databaseFile;

	public Config(BankingPlugin plugin) {
		plugin.saveDefaultConfig();
		accountCommandName = new AccountCommandName(plugin);
		bankCommandName = new BankCommandName(plugin);
		controlCommandName = new ControlCommandName(plugin);
		interestPayoutTimes = new InterestPayoutTimes(plugin);
		interestRate = new InterestRate(plugin);
		multipliers = new Multipliers(plugin);
		initialInterestDelay = new InitialInterestDelay(plugin);
		countInterestDelayOffline = new CountInterestDelayOffline(plugin);
		allowedOfflinePayouts = new AllowedOfflinePayouts(plugin);
		offlineMultiplierDecrement = new OfflineMultiplierDecrement(plugin);
		withdrawalMultiplierDecrement = new WithdrawalMultiplierDecrement(plugin);
		accountCreationPrice = new AccountCreationPrice(plugin);
		reimburseAccountCreation = new ReimburseAccountCreation(plugin);
		bankCreationPrice = new BankCreationPrice(plugin);
		reimburseBankCreation = new ReimburseBankCreation(plugin);
		minimumBalance = new MinimumBalance(plugin);
		lowBalanceFee = new LowBalanceFee(plugin);
		payOnLowBalance = new PayOnLowBalance(plugin);
		playerBankAccountLimit = new PlayerBankAccountLimit(plugin);
		defaultBankLimit = new DefaultBankLimit(plugin);
		defaultAccountLimit = new DefaultAccountLimit(plugin);
		minimumBankVolume = new MinimumBankVolume(plugin);
		maximumBankVolume = new MaximumBankVolume(plugin);
		stubbornBanks = new StubbornBanks(plugin);
		allowSelfBanking = new AllowSelfBanking(plugin);
		confirmOnRemove = new ConfirmOnRemove(plugin);
		confirmOnRemoveAll = new ConfirmOnRemoveAll(plugin);
		confirmOnTransfer = new ConfirmOnTransfer(plugin);
		trustOnTransfer = new TrustOnTransfer(plugin);
		accountInfoItem = new AccountInfoItem(plugin);
		enableUpdateChecker = new EnableUpdateChecker(plugin);
		enableAccountTransactionLog = new EnableAccountTransactionLog(plugin);
		enableAccountInterestLog = new EnableAccountInterestLog(plugin);
		enableBankIncomeLog = new EnableBankIncomeLog(plugin);
		enableDebugLog = new EnableDebugLog(plugin);
		cleanupLogDays = new CleanupLogDays(plugin);
		enableWorldGuardIntegration = new EnableWorldGuardIntegration(plugin);
		enableGriefPreventionIntegration = new EnableGriefPreventionIntegration(plugin);
		enableWorldEditIntegration = new EnableWorldEditIntegration(plugin);
		worldGuardDefaultFlagValue = new WorldGuardDefaultFlagValue(plugin);
		removeAccountOnError = new RemoveAccountOnError(plugin);
		blacklist = new Blacklist(plugin);
		bankRevenueFunction = new BankRevenueFunction(plugin);
		disabledWorlds = new DisabledWorlds(plugin);
		nameRegex = new NameRegex(plugin);
		enableStartupMessage = new EnableStartupMessage(plugin);
		languageFile = new LanguageFile(plugin);
		databaseFile = new DatabaseFile(plugin);
		plugin.saveConfig();
	}

	/**
	 * Constructs an ordered stream of all configuration values, including allow-override values, in this class.
	 * Care must be taken not to call this method before the values have been initialized.
	 * @return an ordered stream of all configuration values
	 */
	private static Stream<ConfigValue<?, ?>> stream() {
		return Stream.of(
				accountCommandName,
				bankCommandName,
				controlCommandName,
				interestPayoutTimes,
				interestPayoutTimes.getAllowOverride(),
				interestRate,
				interestRate.getAllowOverride(),
				multipliers,
				multipliers.getAllowOverride(),
				initialInterestDelay,
				initialInterestDelay.getAllowOverride(),
				countInterestDelayOffline,
				countInterestDelayOffline.getAllowOverride(),
				allowedOfflinePayouts,
				allowedOfflinePayouts.getAllowOverride(),
				offlineMultiplierDecrement,
				offlineMultiplierDecrement.getAllowOverride(),
				withdrawalMultiplierDecrement,
				withdrawalMultiplierDecrement.getAllowOverride(),
				accountCreationPrice,
				accountCreationPrice.getAllowOverride(),
				reimburseAccountCreation,
				reimburseAccountCreation.getAllowOverride(),
				bankCreationPrice,
				reimburseBankCreation,
				minimumBalance,
				minimumBalance.getAllowOverride(),
				lowBalanceFee,
				lowBalanceFee.getAllowOverride(),
				payOnLowBalance,
				payOnLowBalance.getAllowOverride(),
				playerBankAccountLimit,
				playerBankAccountLimit.getAllowOverride(),
				defaultBankLimit,
				defaultAccountLimit,
				minimumBankVolume,
				maximumBankVolume,
				stubbornBanks,
				allowSelfBanking,
				confirmOnRemove,
				confirmOnRemoveAll,
				confirmOnTransfer,
				trustOnTransfer,
				accountInfoItem,
				enableUpdateChecker,
				enableAccountTransactionLog,
				enableAccountInterestLog,
				enableBankIncomeLog,
				enableDebugLog,
				cleanupLogDays,
				enableWorldGuardIntegration,
				enableGriefPreventionIntegration,
				enableWorldEditIntegration,
				worldGuardDefaultFlagValue,
				removeAccountOnError,
				blacklist,
				bankRevenueFunction,
				disabledWorlds,
				nameRegex,
				enableStartupMessage,
				languageFile,
				databaseFile
		);
	}

	public static List<String> matchPath(String input) {
		return stream()
				.map(ConfigValue::getPath)
				.filter(path -> Utils.containsIgnoreCase(path, input))
				.sorted()
				.collect(Collectors.toList());
	}

	public static ConfigValue<?, ?> getByPath(String path) {
		return stream()
				.filter(value -> value.getPath().equalsIgnoreCase(path))
				.findFirst()
				.orElse(null);
	}

	public void reload() {
		stream().forEach(ConfigValue::reload);
	}

}
