package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Config {

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
    public static String commandNameBank;

    /**
     * The account command of BankingPlugin <i>(default: account)</i>
     **/
    public static String commandNameAccount;

    /**
     * The control command of BankingPlugin <i>(default: bankingplugin)</i>
     **/
    public static String commandNameControl;

    /**
     * The real-life times for account interest payouts.
     **/
	public static ConfigPair<List<LocalTime>> interestPayoutTimes;
	public static ConfigPair<List<LocalTime>> getInterestPayoutTimes() { return interestPayoutTimes; }

    /**
     * The default baseline account interest rate.
     **/
	public static ConfigPair<Double> interestRate;
	public static ConfigPair<Double> getInterestRate() { return interestRate; }

    /**
     * The list of default interest multipliers in sequential order.
     **/
	public static ConfigPair<List<Integer>> multipliers;
	public static ConfigPair<List<Integer>> getMultipliers() { return multipliers; }

    /**
	 * The default number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static ConfigPair<Integer> initialInterestDelay;
	public static ConfigPair<Integer> getInitialInterestDelay() { return initialInterestDelay; }

	/**
	 * Whether to decrement the interest delay period while a player is offline.
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static ConfigPair<Boolean> countInterestDelayOffline;
	public static ConfigPair<Boolean> getCountInterestDelayOffline() { return countInterestDelayOffline; }

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static ConfigPair<Integer> allowedOfflinePayouts;
	public static ConfigPair<Integer> getAllowedOfflinePayouts() { return allowedOfflinePayouts; }

    /**
	 * The number of payouts a player is allowed to be offline for (but not
	 * necessarily collect) before their multiplier is reset.
	 **/
	public static ConfigPair<Integer> allowedOfflinePayoutsBeforeReset;
	public static ConfigPair<Integer> getAllowedOfflinePayoutsBeforeReset() { return allowedOfflinePayoutsBeforeReset; }

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static ConfigPair<Integer> offlineMultiplierDecrement;
	public static ConfigPair<Integer> getOfflineMultiplierDecrement() { return offlineMultiplierDecrement; }

    /**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static ConfigPair<Integer> withdrawalMultiplierDecrement;
	public static ConfigPair<Integer> getWithdrawalMultiplierDecrement() { return withdrawalMultiplierDecrement; }

	/**
	 * The price a player has to pay in order to create an account.
	 **/
	public static ConfigPair<Double> accountCreationPrice;
	public static ConfigPair<Double> getAccountCreationPrice() { return accountCreationPrice; }

	/**
	 * Whether the account creation price should be refunded at removal.
	 */
	public static ConfigPair<Boolean> reimburseAccountCreation;
	public static ConfigPair<Boolean> getReimburseAccountCreation() { return reimburseAccountCreation; }

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static double bankCreationPrice;

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public static boolean reimburseBankCreation;

	/**
	 * The default minimum balance.
	 */
	public static ConfigPair<Double> minimumBalance;
	public static ConfigPair<Double> getMinimumBalance() { return minimumBalance; }

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static ConfigPair<Double> lowBalanceFee;
	public static ConfigPair<Double> getLowBalanceFee() { return lowBalanceFee; }

	/**
	 * Whether or not accounts still earn interest on a balance lower than the minimum.
	 */
	public static ConfigPair<Boolean> payOnLowBalance;
	public static ConfigPair<Boolean> getPayOnLowBalance() { return payOnLowBalance; }

	/**
	 * The default account limit per player per bank.
	 */
	public static ConfigPair<Integer> playerBankAccountLimit;
	public static ConfigPair<Integer> getPlayerBankAccountLimit() { return playerBankAccountLimit; }

    /**
     * The default bank ownership limit for players whose limit is not set via a permission.
     **/
	public static int defaultBankLimit;

    /**
     * The default account ownership limit for players whose limit is not set via a permission.
     **/
	public static int defaultAccountLimit;

	/**
	 * The minimum bank volume.
	 */
	public static int minimumBankVolume;

	/**
	 * The default bank volume limit for players whose limit is not set via a
	 * permission.
	 */
	public static long maximumBankVolume;

	/**
	 * Determines how default bank configuration values should behave on server default change.
	 */
	public static boolean stubbornBanks;

	/**
	 * Whether a bank owner should be allowed to create an account at their own
	 * bank.
	 */
	public static boolean allowSelfBanking;

	/**
	 * Whether remove requests should be confirmed.
	 */
	public static boolean confirmOnRemove;

	/**
	 * Whether to require confirmation (second execution) of removeall commands on
	 * the server.
	 */
	public static boolean confirmOnRemoveAll;

	/**
	 * Whether account transfer requests should be confirmed.
	 */
	public static boolean confirmOnTransfer;

	/**
	 * Whether bank or account owners should be automatically added as co-owners
	 * when ownership is transferred to another player.
	 */
	public static boolean trustOnTransfer;

	/**
	 * The item with which a player can click an account chest to retrieve information.
	 **/
	public static ItemStack accountInfoItem;

    /**
     * <p>Whether the update checker should run on start and notify players on join.</p>
     * The command is not affected by this setting and will continue to check for updates.
     **/
    public static boolean enableUpdateChecker;

    /**
     * Whether account deposits and withdrawals should be logged in the database.
     **/
	public static boolean enableAccountTransactionLog;

	/**
	 * Whether interest payouts should be logged in the database.
	 **/
	public static boolean enableAccountInterestLog;

	/**
	 * Whether bank profits and losses should be logged in the database.
	 */
	public static boolean enableBankProfitLog;

	/**
	 * Whether low balance fees should be logged in the database.
	 */
	public static boolean enableLowBalanceFeeLog;

	/**
	 * Whether the debug log file should be created.
	 **/
	public static boolean enableDebugLog;

    /**
     * <p>Sets the time limit for cleaning up the deposit-withdrawal log in days</p>
     *
     * If this is equal to {@code 0}, the log will not be cleaned.
     **/
	public static int cleanupLogDays;

    /**
     * Whether GriefPrevention integration should be enabled.
     **/
    public static boolean enableGriefPreventionIntegration;

	/**
	 * Whether WorldEdit integration should be enabled.
	 **/
	public static boolean enableWorldEditIntegration;

	/**
	 * Whether WorldGuard integration should be enabled.
	 **/
	public static boolean enableWorldGuardIntegration;

	/**
	 * The default value for the custom WorldGuard flag 'create-bank'
	 **/
	public static boolean wgAllowCreateBankDefault;

    /**
	 * <p>
	 * Whether accounts should automatically be removed from the database if an
	 * error occurs while loading
	 * </p>
	 * (e.g. when no chest is found at an account's location)
	 */
    public static boolean removeAccountOnError;

	/**
	 * <p>
	 * List containing items for which a player will earn no interest in their
	 * account.
	 * </p>
	 * If this list contains an item (e.g "STONE", "STONE:1"), it's in the
	 * blacklist.
	 **/
    public static List<String> blacklist;

	/**
	 * The bank revenue function.
	 */
	public static String bankRevenueFunction;

	/**
	 * Worlds where banking should be disabled
	 */
	public static List<String> disabledWorlds;

	/**
	 * Whether to enable in-game mail from the plugin.
	 */
	public static boolean enableMail;

	/**
	 * The regex pattern that bank names and account nicknames should be matched
	 * against.
	 */
	public static String nameRegex;

	/**
	 * The language file to use.
	 */
	public static String languageFile;

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
     * @param property Property to change
     * @param value    Value to set
     */
    public void set(String property, String value) {
    	boolean set = attemptParse(value, v -> plugin.getConfig().set(property, v));
        if (!set)
        	if (property.equalsIgnoreCase("interest-payout-times.default"))
        		plugin.getConfig().set(property, Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				plugin.getConfig().set(property, value);

		update(property, value);
    }

    /**
     * Add a value to a list in the config.yml.
     * If the list does not exist, a new list with the given value will be created
     *
     * @param property Location of the list
     * @param value    Value to add
     */
	public void add(String property, String value) {
		List list = Utils.nonNull(plugin.getConfig().getList(property), ArrayList::new);
		boolean added = attemptParse(value, v -> list.add(v));
		if (!added)
			if (property.equalsIgnoreCase("interest-payout-times.default"))
				list.addAll(Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				list.add(value);

		update(property, value);
    }

    public void remove(String property, String value) {
		List list = Utils.nonNull(plugin.getConfig().getList(property), ArrayList::new);
		boolean removed = attemptParse(value, v -> list.remove(v));
		if (!removed)
			if (property.equalsIgnoreCase("interest-payout-times.default"))
				list.removeAll(Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				list.remove(value);

		update(property, value);
    }

    private boolean attemptParse(String value, Consumer<Object> listModifier) {

		try {
			listModifier.accept(Integer.parseInt(value));
			return true;
		} catch (NumberFormatException ignored) { /* Value not an integer */ }

		try {
			listModifier.accept(Double.parseDouble(value));
			return true;
		} catch (NumberFormatException ignored) { /* Value not a double */ }

		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			listModifier.accept(Boolean.parseBoolean(value));
			return true;
		}

		return false;
	}

	private void update(String property, String value) {
		Bukkit.getPluginManager().callEvent(new PluginConfigureEvent(plugin, property, value));
		if (property.endsWith(".default") || property.endsWith(".ignore-override"))
			plugin.getBankRepository().getAll().forEach(Bank::notifyObservers);
		plugin.saveConfig();
		reload(false, true, false);
	}

    /**
     * Reload the configuration values from config.yml
     */
	public void reload(boolean firstLoad, boolean langReload, boolean showMessages) {
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        commandNameBank = config.getString("command-names.bank", "bank");
        commandNameAccount = config.getString("command-names.account", "account");
		commandNameControl = config.getString("command-names.control", "bp");
		interestPayoutTimes = new ConfigPair<>(
				config.getBoolean("interest-payout-times.allow-override", true),
				config.getStringList("interest-payout-times.default").stream()
						.map(Config::parseLocalTime)
						.filter(Objects::nonNull)
						.distinct()
						.sorted()
						.collect(Collectors.toList())
		);
		if (plugin.isEnabled())
			plugin.getScheduler().scheduleAll();

		interestRate = new ConfigPair<>(
				config.getBoolean("interest-rate.allow-override", true),
				Math.abs(config.getDouble("interest-rate.default", 0.01))
		);

		multipliers = new ConfigPair<>(
				config.getBoolean("interest-multipliers.allow-override", true),
				Utils.ternary(
						config.getIntegerList("interest-multipliers.default"),
						() -> Collections.singletonList(1),
						list -> !list.isEmpty()
				)
		);

		initialInterestDelay = new ConfigPair<>(
				config.getBoolean("initial-interest-delay.allow-override", true),
				Math.abs(config.getInt("initial-interest-delay.default", 0))
		);

		countInterestDelayOffline = new ConfigPair<>(
				config.getBoolean("count-interest-delay-offline.allow-override", false),
				config.getBoolean("count-interest-delay-offline.default", false)
		);

		allowedOfflinePayouts = new ConfigPair<>(
				config.getBoolean("allowed-offline-payouts.allow-override", true),
				Math.abs(config.getInt("allowed-offline-payouts.default", 1))
		);

		allowedOfflinePayoutsBeforeReset = new ConfigPair<>(
				config.getBoolean("allowed-offline-payouts-before-multiplier-reset.allow-override", true),
				Math.abs(config.getInt("allowed-offline-payouts-before-multiplier-reset.default", 1))
		);

		offlineMultiplierDecrement = new ConfigPair<>(
				config.getBoolean("offline-multiplier-decrement.allow-override", true),
				Math.abs(config.getInt("offline-multiplier-decrement.default", 0))
		);

		withdrawalMultiplierDecrement = new ConfigPair<>(
				config.getBoolean("withdrawal-multiplier-decrement.allow-override", true),
				Math.abs(config.getInt("withdrawal-multiplier-decrement.default", 1))
		);

		accountInfoItem = parseItemStack(config.getString("account-info-item"));

		accountCreationPrice = new ConfigPair<>(
				config.getBoolean("creation-prices.account.allow-override", true),
				Math.abs(config.getDouble("creation-prices.account.default", 2500.0))
		);

		reimburseAccountCreation = new ConfigPair<>(
				config.getBoolean("reimburse-account-creation.allow-override", true),
				config.getBoolean("reimburse-account-creation.default", false)
		);

		bankCreationPrice = Math.abs(config.getDouble("bank-creation-price", 100000.0));
		reimburseBankCreation = config.getBoolean("reimburse-bank-creation", false);

		minimumBalance = new ConfigPair<>(
				config.getBoolean("minimum-account-balance.allow-override", true),
				Math.abs(config.getDouble("minimum-account-balance.default", 1000.0))
		);

		lowBalanceFee = new ConfigPair<>(
				config.getBoolean("low-balance-fee.allow-override", true),
				Math.abs(config.getDouble("low-balance-fee.default", 2000.0))
		);

		payOnLowBalance = new ConfigPair<>(
				config.getBoolean("pay-interest-on-low-balance.allow-override", true),
				config.getBoolean("pay-interest-on-low-balance.default", false)
		);

		playerBankAccountLimit = new ConfigPair<>(
				config.getBoolean("player-bank-account-limit.allow-override", true),
				config.getInt("player-bank-account-limit.default", 1)
		);

		defaultBankLimit = config.getInt("default-limits.bank", 1);
		defaultAccountLimit = config.getInt("default-limits.account", 1);
		minimumBankVolume = Math.max(config.getInt("bank-size-limits.minimum", 125), 0);
		maximumBankVolume = Math.max(config.getLong("bank-size-limits.maximum", 100000L), 0);
		stubbornBanks = config.getBoolean("stubborn-banks", false);
		allowSelfBanking = config.getBoolean("allow-self-banking", false);
		confirmOnRemove = config.getBoolean("confirm-on-remove", true);
		confirmOnRemoveAll = config.getBoolean("confirm-on-removeall", true);
		confirmOnTransfer = config.getBoolean("confirm-on-transfer", true);
		trustOnTransfer = config.getBoolean("trust-on-transfer", false);
        enableUpdateChecker = config.getBoolean("enable-update-checker", true);
		enableAccountTransactionLog = config.getBoolean("enable-account-transaction-log", true);
		enableAccountInterestLog = config.getBoolean("enable-account-interest-log", true);
		enableBankProfitLog = config.getBoolean("enable-bank-profit-log", true);
		enableDebugLog = config.getBoolean("enable-debug-log", true);
		cleanupLogDays = config.getInt("cleanup-log-days", 30);
        enableWorldGuardIntegration = config.getBoolean("enable-worldguard-integration", true);
        enableGriefPreventionIntegration = config.getBoolean("enable-griefprevention-integration", true);
		enableWorldEditIntegration = config.getBoolean("enable-worldedit-integration", true);
        removeAccountOnError = config.getBoolean("remove-account-on-error", true);
        blacklist = config.getStringList("blacklist");
		bankRevenueFunction = config.getString("bank-revenue-function", "(0.10 * x) * (1 - g) * ln(n)");
		wgAllowCreateBankDefault = config.getBoolean("worldguard-default-flag-value", false);
		disabledWorlds = config.getStringList("disabled-worlds");
		enableMail = config.getBoolean("enable-mail", true);
		languageFile = config.getString("language-file", "en_US");
		nameRegex = config.getString("name-regex", "");

		if (firstLoad || langReload)
			loadLanguageConfig(showMessages);
		if (!firstLoad && langReload)
			LangUtils.reload();
    }

    public static class ConfigPair<K> extends Pair<Boolean, K> {
		private ConfigPair(Boolean b, K k) {
			super(b, k);
		}
		public boolean isOverridable() { return super.getFirst(); }
		public K getDefault() { return super.getSecond(); }
	}

	private static LocalTime parseLocalTime(String string) {
		try {
			return LocalTime.parse(string);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private static ItemStack parseItemStack(String string) {
		return Optional.ofNullable(string).map(Material::getMaterial).map(ItemStack::new).orElse(null);
	}

	private void loadLanguageConfig(boolean showMessages) {
		languageConfig = new LanguageConfig(plugin, showMessages);
		File langFolder = new File(plugin.getDataFolder(), "lang");

		if (!(new File(langFolder, "en_US.lang")).exists())
			plugin.saveResource("lang/en_US.lang", false);

		if (!(new File(langFolder, "de_DE.lang")).exists())
			plugin.saveResource("lang/de_DE.lang", false);

		File specifiedLangFile = new File(langFolder, languageFile + ".lang");
		if (specifiedLangFile.exists()) {
			try {
				if (showMessages)
					plugin.getLogger().info("Using locale \"" + specifiedLangFile.getName().substring(0, specifiedLangFile.getName().length() - 5) + "\"");
				languageConfig.load(specifiedLangFile);
			} catch (IOException e) {
				if (showMessages)
					plugin.getLogger().warning("Using default language values.");
				plugin.debug("Using default language values (#1)");
				plugin.debug(e);
			}
		} else {
			File defaultLangFile = new File(langFolder, "en_US.lang");
			if (defaultLangFile.exists()) {
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
				Reader reader = plugin.getTextResourceMirror("lang/" + specifiedLangFile.getName());
				if (reader == null)
					reader = plugin.getTextResourceMirror("lang/en_US.lang");

				if (reader != null) {
					try (BufferedReader br = new BufferedReader(reader)) {
						languageConfig.loadFromStream(br.lines());
						if (showMessages)
							plugin.getLogger().info("Using locale \"" + specifiedLangFile.getName()
									.substring(0, specifiedLangFile.getName().length() - 5) + "\" (Streamed from jar file)");
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
