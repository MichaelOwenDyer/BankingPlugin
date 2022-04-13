package com.monst.bankingplugin.configuration;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.values.*;
import com.monst.pluginconfiguration.ConfigurationValue;

import java.util.stream.Stream;

public final class Configuration {

	/**
	 * The account command of BankingPlugin <i>(default: account)</i>
	 **/
	public final AccountCommandName accountCommandName;

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
	public final BankCommandName bankCommandName;

    /**
     * The control command of BankingPlugin <i>(default: bp)</i>
     **/
    public final PluginCommandName pluginCommandName;

    /**
     * The real-life times for account interest payouts.
     **/
	public final InterestPayoutTimes interestPayoutTimes;

    /**
     * The default baseline account interest rate.
     **/
	public final InterestRate interestRate;

    /**
     * The list of default interest multipliers in sequential order.
     **/
	public final InterestMultipliers interestMultipliers;

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public final AllowedOfflinePayouts allowedOfflinePayouts;

    /**
	 * The behavior of account interest multipliers during an interest event when all trusted players are offline.
	 **/
	public final OfflineMultiplierDecrement offlineMultiplierDecrement;

	/**
	 * The behavior of account interest multipliers when a withdrawal is made.
	 **/
	public final WithdrawalMultiplierDecrement withdrawalMultiplierDecrement;

	/**
	 * The price a player has to pay in order to open an account.
	 **/
	public final AccountCreationPrice accountCreationPrice;

	/**
	 * Whether the account creation price should be refunded at removal.
	 */
	public final ReimburseAccountCreation reimburseAccountCreation;

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public final BankCreationPrice bankCreationPrice;

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public final ReimburseBankCreation reimburseBankCreation;

	/**
	 * The default minimum balance.
	 */
	public final MinimumBalance minimumBalance;

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public final LowBalanceFee lowBalanceFee;

	/**
	 * Whether accounts still earn interest on a low balance.
	 */
	public final PayOnLowBalance payOnLowBalance;

	/**
	 * The default account limit per player per bank.
	 */
	public final PlayerBankAccountLimit playerBankAccountLimit;

    /**
     * The default bank ownership limit for players whose limit is not set via a permission.
     **/
	public final DefaultBankLimit defaultBankLimit;

    /**
     * The default account ownership limit for players whose limit is not set via a permission.
     **/
	public final DefaultAccountLimit defaultAccountLimit;

	/**
	 * The minimum bank volume.
	 */
	public final MinimumBankVolume minimumBankVolume;

	/**
	 * The default bank volume limit for players whose limit is not set via a
	 * permission.
	 */
	public final MaximumBankVolume maximumBankVolume;

	/**
	 * Determines how default bank configuration values should behave on server default change.
	 */
	public final StickyDefaults stickyDefaults;

	/**
	 * Whether a bank owner should be allowed to open an account at their own bank.
	 */
	public final AllowSelfBanking allowSelfBanking;

	/**
	 * Whether remove requests should be confirmed.
	 */
	public final ConfirmOnRemove confirmOnRemove;

	/**
	 * Whether to require confirmation (second execution) of removeall commands on
	 * the server.
	 */
	public final ConfirmOnRemoveAll confirmOnRemoveAll;

	/**
	 * Whether account transfer requests should be confirmed.
	 */
	public final ConfirmOnTransfer confirmOnTransfer;

	/**
	 * Whether bank or account owners should be automatically added as co-owners
	 * when ownership is transferred to another player.
	 */
	public final TrustOnTransfer trustOnTransfer;

	/**
	 * The item with which a player can click an account chest to retrieve information.
	 **/
	public final AccountInfoItem accountInfoItem;

    /**
     * Whether the plugin should check for updates on start.
     **/
	public final EnableStartupUpdateCheck enableStartupUpdateCheck;

    /**
     * Whether updates should be downloaded automatically or require confirmation.
     **/
	public final DownloadUpdatesAutomatically downloadUpdatesAutomatically;

	/**
	 * Which updates to ignore.
	 */
	public final IgnoreUpdatesContaining ignoreUpdatesContaining;

	/**
	 * Whether the debug log file should be created.
	 **/
	public final EnableDebugLog enableDebugLog;

    /**
     * <p>Sets the time limit for cleaning up the deposit-withdrawal log in days</p>
     *
     * If this is equal to {@code 0}, the log will not be cleaned.
     **/
	public final CleanupLogDays cleanupLogDays;

	/**
	 * Whether WorldGuard integration should be enabled.
	 **/
	public final EnableWorldGuardIntegration enableWorldGuardIntegration;

    /**
     * Whether GriefPrevention integration should be enabled.
     **/
	public final EnableGriefPreventionIntegration enableGriefPreventionIntegration;

	/**
	 * Whether WorldEdit integration should be enabled.
	 **/
	public final EnableWorldEditIntegration enableWorldEditIntegration;

	/**
	 * The default value for the custom WorldGuard flag 'create-bank'
	 **/
	public final WorldGuardDefaultFlagValue worldGuardDefaultFlagValue;

	/**
	 * <p>
	 * List containing items for which a player will earn no interest in their
	 * account.
	 * </p>
	 * If this list contains an item (e.g "STONE", "STONE:1"), it's in the
	 * blacklist.
	 **/
    public final Blacklist blacklist;

	/**
	 * The bank revenue function.
	 */
	public final BankRevenueExpression bankRevenueExpression;

	/**
	 * Worlds where banking should be disabled
	 */
	public final DisabledWorlds disabledWorlds;

	/**
	 * The regex pattern that bank names and account nicknames should be matched
	 * against.
	 */
	public final NameRegex nameRegex;

	/**
	 * Whether to enable the plugin startup message in the console upon server start.
	 */
	public final EnableStartupMessage enableStartupMessage;

	/**
	 * The language file to use.
	 */
	public final LanguageFile languageFile;

	/**
	 * The path to use for the database file.
	 */
	public final DatabaseFile databaseFile;

	public Configuration(BankingPlugin plugin) {
		plugin.saveDefaultConfig();
		accountCommandName = new AccountCommandName(plugin);
		bankCommandName = new BankCommandName(plugin);
		pluginCommandName = new PluginCommandName(plugin);
		interestPayoutTimes = new InterestPayoutTimes(plugin);
		interestRate = new InterestRate(plugin);
		interestMultipliers = new InterestMultipliers(plugin);
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
		stickyDefaults = new StickyDefaults(plugin);
		allowSelfBanking = new AllowSelfBanking(plugin);
		confirmOnRemove = new ConfirmOnRemove(plugin);
		confirmOnRemoveAll = new ConfirmOnRemoveAll(plugin);
		confirmOnTransfer = new ConfirmOnTransfer(plugin);
		trustOnTransfer = new TrustOnTransfer(plugin);
		accountInfoItem = new AccountInfoItem(plugin);
		enableStartupUpdateCheck = new EnableStartupUpdateCheck(plugin);
		downloadUpdatesAutomatically = new DownloadUpdatesAutomatically(plugin);
		ignoreUpdatesContaining = new IgnoreUpdatesContaining(plugin);
		enableDebugLog = new EnableDebugLog(plugin);
		cleanupLogDays = new CleanupLogDays(plugin);
		enableWorldGuardIntegration = new EnableWorldGuardIntegration(plugin);
		enableGriefPreventionIntegration = new EnableGriefPreventionIntegration(plugin);
		enableWorldEditIntegration = new EnableWorldEditIntegration(plugin);
		worldGuardDefaultFlagValue = new WorldGuardDefaultFlagValue(plugin);
		blacklist = new Blacklist(plugin);
		bankRevenueExpression = new BankRevenueExpression(plugin);
		disabledWorlds = new DisabledWorlds(plugin);
		nameRegex = new NameRegex(plugin);
		enableStartupMessage = new EnableStartupMessage(plugin);
		languageFile = new LanguageFile(plugin);
		databaseFile = new DatabaseFile(plugin);
		plugin.saveConfig();
	}

	/**
	 * Constructs an ordered stream of all configuration values, including allow-override values, in this class.
	 * @return an ordered stream of all configuration values
	 */
	public Stream<ConfigurationValue<?>> stream() {
		return Stream.of(
				accountCommandName,
				bankCommandName,
				pluginCommandName,
				interestPayoutTimes,
				interestPayoutTimes.allowOverride,
				interestRate,
				interestRate.allowOverride,
                interestMultipliers,
				interestMultipliers.allowOverride,
				allowedOfflinePayouts,
				allowedOfflinePayouts.allowOverride,
				offlineMultiplierDecrement,
				offlineMultiplierDecrement.allowOverride,
				withdrawalMultiplierDecrement,
				withdrawalMultiplierDecrement.allowOverride,
				accountCreationPrice,
				accountCreationPrice.allowOverride,
				reimburseAccountCreation,
				reimburseAccountCreation.allowOverride,
				bankCreationPrice,
				reimburseBankCreation,
				minimumBalance,
				minimumBalance.allowOverride,
				lowBalanceFee,
				lowBalanceFee.allowOverride,
				payOnLowBalance,
				payOnLowBalance.allowOverride,
				playerBankAccountLimit,
				playerBankAccountLimit.allowOverride,
				defaultBankLimit,
				defaultAccountLimit,
				minimumBankVolume,
				maximumBankVolume,
				stickyDefaults,
				allowSelfBanking,
				confirmOnRemove,
				confirmOnRemoveAll,
				confirmOnTransfer,
				trustOnTransfer,
				accountInfoItem,
				enableStartupUpdateCheck,
				downloadUpdatesAutomatically,
				enableDebugLog,
				cleanupLogDays,
				enableWorldGuardIntegration,
				enableGriefPreventionIntegration,
				enableWorldEditIntegration,
				worldGuardDefaultFlagValue,
				blacklist,
                bankRevenueExpression,
				disabledWorlds,
				nameRegex,
				enableStartupMessage,
				languageFile,
				databaseFile
		);
	}

	public void reload() {
		stream().forEach(ConfigurationValue::reload);
	}

}
