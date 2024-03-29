package com.monst.bankingplugin.configuration;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.values.*;

public final class Configuration extends ConfigurationRoot {
	
	/**
	 * Contains the names of this plugin's commands.
	 */
	public final CommandNames commandNames;

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
	public final PlayerAccountPerBankLimit playerAccountPerBankLimit;
	
	/**
	 * Contains default limits for ownership of accounts and banks.
	 */
	public final DefaultLimits defaultLimits;
	
	/**
	 * Contains the maximum and minimum bank volume limits.
	 */
	public final BankSizeLimits bankSizeLimits;

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
	 * Contains whether to enable the plugin's integrations with WorldGuard, WorldEdit and GriefPrevention.
	 */
	public final Integrations integrations;

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
		super(plugin, "config.yml");
		
		this.enableDebugLog = addChild(new EnableDebugLog(plugin)); // Must be first to ensure debug log is created if enabled
		this.commandNames = addChild(new CommandNames(plugin));
		this.interestPayoutTimes = addChild(new InterestPayoutTimes(plugin));
		this.interestRate = addChild(new InterestRate(plugin));
		this.interestMultipliers = addChild(new InterestMultipliers(plugin));
		this.allowedOfflinePayouts = addChild(new AllowedOfflinePayouts(plugin));
		this.offlineMultiplierDecrement = addChild(new OfflineMultiplierDecrement(plugin));
		this.withdrawalMultiplierDecrement = addChild(new WithdrawalMultiplierDecrement(plugin));
		this.accountCreationPrice = addChild(new AccountCreationPrice(plugin));
		this.reimburseAccountCreation = addChild(new ReimburseAccountCreation(plugin));
		this.bankCreationPrice = addChild(new BankCreationPrice(plugin));
		this.reimburseBankCreation = addChild(new ReimburseBankCreation(plugin));
		this.minimumBalance = addChild(new MinimumBalance(plugin));
		this.lowBalanceFee = addChild(new LowBalanceFee(plugin));
		this.payOnLowBalance = addChild(new PayOnLowBalance(plugin));
		this.playerAccountPerBankLimit = addChild(new PlayerAccountPerBankLimit(plugin));
		this.defaultLimits = addChild(new DefaultLimits(plugin));
		this.bankSizeLimits = addChild(new BankSizeLimits(plugin));
		this.stickyDefaults = addChild(new StickyDefaults(plugin));
		this.allowSelfBanking = addChild(new AllowSelfBanking(plugin));
		this.confirmOnRemove = addChild(new ConfirmOnRemove(plugin));
		this.confirmOnRemoveAll = addChild(new ConfirmOnRemoveAll(plugin));
		this.confirmOnTransfer = addChild(new ConfirmOnTransfer(plugin));
		this.trustOnTransfer = addChild(new TrustOnTransfer(plugin));
		this.accountInfoItem = addChild(new AccountInfoItem(plugin));
		this.enableStartupUpdateCheck = addChild(new EnableStartupUpdateCheck(plugin));
		this.downloadUpdatesAutomatically = addChild(new DownloadUpdatesAutomatically(plugin));
		this.ignoreUpdatesContaining = addChild(new IgnoreUpdatesContaining(plugin));
		this.cleanupLogDays = addChild(new CleanupLogDays(plugin));
		this.integrations = addChild(new Integrations(plugin));
		this.worldGuardDefaultFlagValue = addChild(new WorldGuardDefaultFlagValue(plugin));
		this.blacklist = addChild(new Blacklist(plugin));
		this.bankRevenueExpression = addChild(new BankRevenueExpression(plugin));
		this.disabledWorlds = addChild(new DisabledWorlds(plugin));
		this.nameRegex = addChild(new NameRegex(plugin));
		this.enableStartupMessage = addChild(new EnableStartupMessage(plugin));
		this.languageFile = addChild(new LanguageFile(plugin));
		this.databaseFile = addChild(new DatabaseFile(plugin));
		super.reload();
	}
	
}
