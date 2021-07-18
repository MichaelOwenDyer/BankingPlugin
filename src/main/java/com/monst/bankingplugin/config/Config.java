package com.monst.bankingplugin.config;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.overridable.*;
import com.monst.bankingplugin.config.values.simple.*;
import com.monst.bankingplugin.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {

	/**
	 * The account command of BankingPlugin <i>(default: account)</i>
	 **/
	public static AccountCommandName accountCommandName = new AccountCommandName();

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
    public static BankCommandName bankCommandName = new BankCommandName();

    /**
     * The control command of BankingPlugin <i>(default: bp)</i>
     **/
    public static ControlCommandName controlCommandName = new ControlCommandName();

    /**
     * The real-life times for account interest payouts.
     **/
	public static InterestPayoutTimes interestPayoutTimes = new InterestPayoutTimes();

    /**
     * The default baseline account interest rate.
     **/
	public static InterestRate interestRate = new InterestRate();

    /**
     * The list of default interest multipliers in sequential order.
     **/
	public static Multipliers multipliers = new Multipliers();

    /**
	 * The default number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static InitialInterestDelay initialInterestDelay = new InitialInterestDelay();

	/**
	 * Whether to decrement the interest delay period while a player is offline.
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static CountInterestDelayOffline countInterestDelayOffline = new CountInterestDelayOffline();

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static AllowedOfflinePayouts allowedOfflinePayouts = new AllowedOfflinePayouts();

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static OfflineMultiplierDecrement offlineMultiplierDecrement = new OfflineMultiplierDecrement();

	/**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static WithdrawalMultiplierDecrement withdrawalMultiplierDecrement = new WithdrawalMultiplierDecrement();

	/**
	 * The price a player has to pay in order to create an account.
	 **/
	public static AccountCreationPrice accountCreationPrice = new AccountCreationPrice();

	/**
	 * Whether the account creation price should be refunded at removal.
	 */
	public static ReimburseAccountCreation reimburseAccountCreation = new ReimburseAccountCreation();

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static BankCreationPrice bankCreationPrice = new BankCreationPrice();

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public static ReimburseBankCreation reimburseBankCreation = new ReimburseBankCreation();

	/**
	 * The default minimum balance.
	 */
	public static MinimumBalance minimumBalance = new MinimumBalance();

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static LowBalanceFee lowBalanceFee = new LowBalanceFee();

	/**
	 * Whether or not accounts still earn interest on a balance lower than the minimum.
	 */
	public static PayOnLowBalance payOnLowBalance = new PayOnLowBalance();

	/**
	 * The default account limit per player per bank.
	 */
	public static PlayerBankAccountLimit playerBankAccountLimit = new PlayerBankAccountLimit();

    /**
     * The default bank ownership limit for players whose limit is not set via a permission.
     **/
	public static DefaultBankLimit defaultBankLimit = new DefaultBankLimit();

    /**
     * The default account ownership limit for players whose limit is not set via a permission.
     **/
	public static DefaultAccountLimit defaultAccountLimit = new DefaultAccountLimit();

	/**
	 * The minimum bank volume.
	 */
	public static MinimumBankVolume minimumBankVolume = new MinimumBankVolume();

	/**
	 * The default bank volume limit for players whose limit is not set via a
	 * permission.
	 */
	public static MaximumBankVolume maximumBankVolume = new MaximumBankVolume();

	/**
	 * Determines how default bank configuration values should behave on server default change.
	 */
	public static StubbornBanks stubbornBanks = new StubbornBanks();

	/**
	 * Whether a bank owner should be allowed to create an account at their own
	 * bank.
	 */
	public static AllowSelfBanking allowSelfBanking = new AllowSelfBanking();

	/**
	 * Whether remove requests should be confirmed.
	 */
	public static ConfirmOnRemove confirmOnRemove = new ConfirmOnRemove();

	/**
	 * Whether to require confirmation (second execution) of removeall commands on
	 * the server.
	 */
	public static ConfirmOnRemoveAll confirmOnRemoveAll = new ConfirmOnRemoveAll();

	/**
	 * Whether account transfer requests should be confirmed.
	 */
	public static ConfirmOnTransfer confirmOnTransfer = new ConfirmOnTransfer();

	/**
	 * Whether bank or account owners should be automatically added as co-owners
	 * when ownership is transferred to another player.
	 */
	public static TrustOnTransfer trustOnTransfer = new TrustOnTransfer();

	/**
	 * The item with which a player can click an account chest to retrieve information.
	 **/
	public static AccountInfoItem accountInfoItem = new AccountInfoItem();

    /**
     * <p>Whether the update checker should run on start and notify players on join.</p>
     * The command is not affected by this setting and will continue to check for updates.
     **/
	public static EnableUpdateChecker enableUpdateChecker = new EnableUpdateChecker();

    /**
     * Whether account deposits and withdrawals should be logged in the database.
     **/
	public static EnableAccountTransactionLog enableAccountTransactionLog = new EnableAccountTransactionLog();

	/**
	 * Whether interest payouts should be logged in the database.
	 **/
	public static EnableAccountInterestLog enableAccountInterestLog = new EnableAccountInterestLog();

	/**
	 * Whether bank income should be logged in the database.
	 */
	public static EnableBankIncomeLog enableBankIncomeLog = new EnableBankIncomeLog();

	/**
	 * Whether the debug log file should be created.
	 **/
	public static EnableDebugLog enableDebugLog = new EnableDebugLog();

    /**
     * <p>Sets the time limit for cleaning up the deposit-withdrawal log in days</p>
     *
     * If this is equal to {@code 0}, the log will not be cleaned.
     **/
	public static CleanupLogDays cleanupLogDays = new CleanupLogDays();

	/**
	 * Whether WorldGuard integration should be enabled.
	 **/
	public static EnableWorldGuardIntegration enableWorldGuardIntegration = new EnableWorldGuardIntegration();

    /**
     * Whether GriefPrevention integration should be enabled.
     **/
	public static EnableGriefPreventionIntegration enableGriefPreventionIntegration = new EnableGriefPreventionIntegration();

	/**
	 * Whether WorldEdit integration should be enabled.
	 **/
	public static EnableWorldEditIntegration enableWorldEditIntegration = new EnableWorldEditIntegration();

	/**
	 * The default value for the custom WorldGuard flag 'create-bank'
	 **/
	public static WorldGuardDefaultFlagValue worldGuardDefaultFlagValue = new WorldGuardDefaultFlagValue();

    /**
	 * <p>
	 * Whether accounts should automatically be removed from the database if an
	 * error occurs while loading
	 * </p>
	 * (e.g. when no chest is found at an account's location)
	 */
	public static RemoveAccountOnError removeAccountOnError = new RemoveAccountOnError();

	/**
	 * <p>
	 * List containing items for which a player will earn no interest in their
	 * account.
	 * </p>
	 * If this list contains an item (e.g "STONE", "STONE:1"), it's in the
	 * blacklist.
	 **/
    public static Blacklist blacklist = new Blacklist();

	/**
	 * The bank revenue function.
	 */
	public static BankRevenueFunction bankRevenueFunction = new BankRevenueFunction();

	/**
	 * Worlds where banking should be disabled
	 */
	public static DisabledWorlds disabledWorlds = new DisabledWorlds();

	/**
	 * Whether to enable in-game mail from the plugin.
	 */
	public static EnableMail enableMail = new EnableMail();

	/**
	 * The regex pattern that bank names and account nicknames should be matched
	 * against.
	 */
	public static NameRegex nameRegex = new NameRegex();

	/**
	 * Whether to enable the plugin startup message in the console upon server start.
	 */
	public static EnableStartupMessage enableStartupMessage = new EnableStartupMessage();

	/**
	 * The language file to use.
	 */
	public static LanguageFile languageFile = new LanguageFile();

	private static final ConfigValue<?, ?>[] VALUES = new ConfigValue[] {
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
			enableMail,
			nameRegex,
			enableStartupMessage,
			languageFile
	};

	public static List<String> matchPath(String input) {
		return Stream.of(VALUES)
				.map(ConfigValue::getPath)
				.filter(path -> Utils.containsIgnoreCase(path, input))
				.sorted()
				.collect(Collectors.toList());
	}

	public static ConfigValue<?, ?> getByPath(String path) {
		return Stream.of(VALUES)
				.filter(value -> value.getPath().equalsIgnoreCase(path))
				.findFirst()
				.orElse(null);
	}

	public static void reload() {
		for (ConfigValue<?, ?> configValue : VALUES) {
			configValue.forgetLastSeen();
			configValue.get();
		}
	}

}
