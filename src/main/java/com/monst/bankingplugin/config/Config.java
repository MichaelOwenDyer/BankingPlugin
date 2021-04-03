package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.values.ConfigField;
import com.monst.bankingplugin.config.values.overridable.*;
import com.monst.bankingplugin.config.values.simple.*;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.lang.LangUtils;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
    public static BankCommandName bankCommandName = new BankCommandName();

    /**
     * The account command of BankingPlugin <i>(default: account)</i>
     **/
    public static AccountCommandName accountCommandName = new AccountCommandName();

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
	 * The number of payouts a player is allowed to be offline for (but not
	 * necessarily collect) before their multiplier is reset.
	 **/
	public static AllowedOfflinePayoutsBeforeReset allowedOfflinePayoutsBeforeReset = new AllowedOfflinePayoutsBeforeReset();

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
	 * Whether bank profits and losses should be logged in the database.
	 */
	public static EnableBankProfitLog enableBankProfitLog = new EnableBankProfitLog();

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
	 * The language file to use.
	 */
	public static LanguageFile languageFile = new LanguageFile();

	/* ----------------------------------------------- */

	private final BankingPlugin plugin;

	private LanguageConfig languageConfig;

	public Config(BankingPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

		reload(true, true, true);
    }

    public LanguageConfig getLanguageConfig() {
		return languageConfig;
	}

	/**
	 * <p>Set a configuration value</p>
	 * <i>Config is automatically reloaded</i>
	 *
	 * @param field 	Property to change
	 * @param input		Value to set
	 */
	public void set(ConfigField field, String path, String input) throws ArgumentParseException {
		field.getConfigValue().set(path, input);
		Bukkit.getPluginManager().callEvent(new PluginConfigureEvent(plugin, field, input));
		plugin.saveConfig();
		reload(false, true, false);
	}

    /**
     * Reload the configuration values from config.yml
     */
	public void reload(boolean firstLoad, boolean langReload, boolean showMessages) {
        plugin.reloadConfig();

		if (plugin.isEnabled())
			plugin.getScheduler().scheduleAll();

		if (firstLoad || langReload)
			loadLanguageConfig(showMessages);
		if (!firstLoad && langReload)
			LangUtils.reload();
    }

	private void loadLanguageConfig(boolean showMessages) {
		languageConfig = new LanguageConfig(plugin, showMessages);
		Path langFolder = plugin.getDataFolder().toPath().resolve("lang");
		Path defaultLangFile = langFolder.resolve("en_US.lang");

		if (!Files.exists(defaultLangFile))
			plugin.saveResource("lang/en_US.lang", false);

		if (!Files.exists(langFolder.resolve("de_DE.lang")))
			plugin.saveResource("lang/de_DE.lang", false);

		Path specifiedLang = langFolder.resolve(languageFile.get() + ".lang");
		if (Files.exists(specifiedLang)) {
			try {
				if (showMessages)
					plugin.getLogger().info("Using locale \"" + languageFile.get() + "\"");
				languageConfig.load(specifiedLang);
			} catch (IOException e) {
				if (showMessages)
					plugin.getLogger().warning("Using default language values.");
				plugin.debug("Using default language values (#1)");
				plugin.debug(e);
			}
		} else {
			if (Files.exists(defaultLangFile)) {
				try {
					languageConfig.load(defaultLangFile);
					if (showMessages)
						plugin.getLogger().info("Using locale \"en_US\"");
				} catch (IOException e) {
					if (showMessages)
						plugin.getLogger().warning("Using default language values.");
					plugin.debug("Using default language values (#2)");
					plugin.debug(e);
				}
			} else {
				String fileName;
				Reader reader = plugin.getTextResourceMirror(fileName = specifiedLang.toString());
				if (reader == null)
					reader = plugin.getTextResourceMirror(fileName = defaultLangFile.toString());

				if (reader != null) {
					try (BufferedReader br = new BufferedReader(reader)) {
						languageConfig.loadFromStream(br.lines());
						if (showMessages)
							plugin.getLogger().info("Using lang file \"" + fileName + "\" (Streamed from .jar)");
					} catch (IOException e) {
						if (showMessages)
							plugin.getLogger().warning("Using default language values.");
						plugin.debug("Using default language values (#3)");
					}
				} else {
					if (showMessages)
						plugin.getLogger().warning("Using default language values.");
					plugin.debug("Using default language values (#4, Reader is null)");
				}
			}
		}
	}
}
