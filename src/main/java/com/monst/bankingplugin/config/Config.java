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
import java.util.function.Supplier;
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

    /**
     * The default baseline account interest rate.
     **/
	public static ConfigPair<Double> interestRate;

    /**
     * The list of default interest multipliers in sequential order.
     **/
	public static ConfigPair<List<Integer>> multipliers;

    /**
	 * The default number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static ConfigPair<Integer> initialInterestDelay;

	/**
	 * Whether to decrement the interest delay period while a player is offline.
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static ConfigPair<Boolean> countInterestDelayOffline;

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static ConfigPair<Integer> allowedOfflinePayouts;

    /**
	 * The number of payouts a player is allowed to be offline for (but not
	 * necessarily collect) before their multiplier is reset.
	 **/
	public static ConfigPair<Integer> allowedOfflinePayoutsBeforeReset;

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static ConfigPair<Integer> offlineMultiplierDecrement;

    /**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static ConfigPair<Integer> withdrawalMultiplierDecrement;

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static double bankCreationPriceAdmin;
	public static double bankCreationPricePlayer;

    /**
     * The price a player has to pay in order to create an account.
     **/
	public static ConfigPair<Double> accountCreationPrice;

	/**
	 * Whether the account creation price should be refunded at removal.
	 */
	public static ConfigPair<Boolean> reimburseAccountCreation;

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public static boolean reimburseBankCreationAdmin;
	public static boolean reimburseBankCreationPlayer;

	/**
	 * The default minimum balance.
	 */
	public static ConfigPair<Double> minimumBalance;

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static ConfigPair<Double> lowBalanceFee;

	/**
	 * Whether or not accounts still earn interest on a balance lower than the minimum.
	 */
	public static ConfigPair<Boolean> payOnLowBalance;

	/**
	 * The default account limit per player per bank.
	 */
	public static ConfigPair<Integer> playerBankAccountLimit;

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
	public static boolean enableBankRevenueLog;

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

	private LangConfig langConfig;

	public Config(BankingPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

		reload(true, true, true);
    }

    public LangConfig getLanguageConfig() {
		return langConfig;
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
    @SuppressWarnings("all")
	public void add(String property, String value) {
		List list = nonNull(plugin.getConfig().getList(property), ArrayList::new);
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

	@SuppressWarnings("all")
    public void remove(String property, String value) {
		List list = nonNull(plugin.getConfig().getList(property), ArrayList::new);
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

		boolean success = false;
		try {
			listModifier.accept(Integer.parseInt(value));
			success = true;
		} catch (NumberFormatException ignored) { /* Value not an integer */ }

		if (!success)
			try {
				listModifier.accept(Double.parseDouble(value));
				success = true;
			} catch (NumberFormatException ignored) { /* Value not a double */ }

		if (!success)
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				listModifier.accept(Boolean.parseBoolean(value));
				success = true;
			}

		return success;
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

        commandNameBank = nonNull(config.getString("command-names.bank"), "bank");
        commandNameAccount = nonNull(config.getString("command-names.account"), "account");
		commandNameControl = nonNull(config.getString("command-names.control"), "bp");
		interestPayoutTimes = new ConfigPair<>(
				config.getBoolean("interest-payout-times.allow-override"),
				nonNull(config.getStringList("interest-payout-times.default").stream()
						.map(Config::convertToLocalTime)
						.filter(Objects::nonNull)
						.distinct()
						.sorted()
						.collect(Collectors.toList()), Collections::emptyList)
		);
		if (plugin.isEnabled())
			plugin.getScheduler().scheduleAll();

		interestRate = new ConfigPair<>(
				config.getBoolean("interest-rate.allow-override"),
				nonNull(Math.abs(config.getDouble("interest-rate.default")), 0.01)
		);

		multipliers = new ConfigPair<>(
				config.getBoolean("interest-multipliers.allow-override"),
				config.getIntegerList("interest-multipliers.default").isEmpty() ?
						Collections.singletonList(1) : config.getIntegerList("interest-multipliers.default")
		);

		initialInterestDelay = new ConfigPair<>(
				config.getBoolean("initial-interest-delay.allow-override"),
				nonNull(Math.abs(config.getInt("initial-interest-delay.default")), 0)
		);

		countInterestDelayOffline = new ConfigPair<>(
				nonNull(config.getBoolean("count-interest-delay-offline.allow-override"), false),
				nonNull(config.getBoolean("count-interest-delay-offline.default"), false)
		);

		allowedOfflinePayouts = new ConfigPair<>(
				config.getBoolean("allowed-offline-payouts.allow-override"),
				nonNull(Math.abs(config.getInt("allowed-offline-payouts.default")), 1)
		);

		allowedOfflinePayoutsBeforeReset = new ConfigPair<>(
				config.getBoolean("allowed-offline-payouts-before-multiplier-reset.allow-override"),
				nonNull(Math.abs(config.getInt("allowed-offline-payouts-before-multiplier-reset.default")), 1)
		);

		offlineMultiplierDecrement = new ConfigPair<>(
				config.getBoolean("offline-multiplier-decrement.allow-override"),
				nonNull(Math.abs(config.getInt("offline-multiplier-decrement.default")), 0)
		);

		withdrawalMultiplierDecrement = new ConfigPair<>(
				config.getBoolean("withdrawal-multiplier-decrement.allow-override"),
				nonNull(Math.abs(config.getInt("withdrawal-multiplier-decrement.default")), 1)
		);

		accountInfoItem = nonNull(
				convertToItemStack(config.getString("account-info-item")),
				() -> new ItemStack(Material.STICK)
		);

		bankCreationPricePlayer = nonNull(Math.abs(config.getDouble("creation-prices.bank.player")), 100000.0);
		bankCreationPriceAdmin = nonNull(Math.abs(config.getDouble("creation-prices.bank.admin")), 0.0);

		accountCreationPrice = new ConfigPair<>(
				config.getBoolean("creation-prices.account.allow-override"),
				nonNull(Math.abs(config.getDouble("creation-prices.account.default")), 2500.0)
		);

		reimburseBankCreationPlayer = nonNull(config.getBoolean("reimburse-creation.bank.player"), false);
		reimburseBankCreationAdmin = nonNull(config.getBoolean("reimburse-creation.bank.admin"), false);

		reimburseAccountCreation = new ConfigPair<>(
				config.getBoolean("reimburse-creation.account.allow-override"),
				nonNull(config.getBoolean("reimburse-creation.account.default"), false)
		);

		minimumBalance = new ConfigPair<>(
				config.getBoolean("minimum-account-balance.allow-override"),
				nonNull(Math.abs(config.getDouble("minimum-account-balance.default")), 1000.0)
		);

		lowBalanceFee = new ConfigPair<>(
				config.getBoolean("low-balance-fee.allow-override"),
				nonNull(Math.abs(config.getDouble("low-balance-fee.default")), 2000.0)
		);

		payOnLowBalance = new ConfigPair<>(
				config.getBoolean("pay-interest-on-low-balance.allow-override"),
				nonNull(config.getBoolean("pay-interest-on-low-balance.default"), false)
		);

		playerBankAccountLimit = new ConfigPair<>(
				config.getBoolean("player-bank-account-limit.allow-override"),
				nonNull(config.getInt("player-bank-account-limit.default"), 1)
		);

		defaultBankLimit = nonNull(config.getInt("default-limits.bank"), 1);
		defaultAccountLimit = nonNull(config.getInt("default-limits.account"), 1);
		minimumBankVolume = Math.max(nonNull(config.getInt("bank-size-limits.minimum"), 125), 0);
		maximumBankVolume = Math.max(nonNull(config.getLong("bank-size-limits.maximum"), 100000L), 0);
		allowSelfBanking = nonNull(config.getBoolean("allow-self-banking"), false);
		confirmOnRemove = nonNull(config.getBoolean("confirm-on-remove"), true);
		confirmOnRemoveAll = nonNull(config.getBoolean("confirm-on-removeall"), true);
		confirmOnTransfer = nonNull(config.getBoolean("confirm-on-transfer"), true);
		trustOnTransfer = nonNull(config.getBoolean("trust-on-transfer"), false);
        enableUpdateChecker = nonNull(config.getBoolean("enable-update-checker"), true);
		enableAccountTransactionLog = nonNull(config.getBoolean("enable-transaction-log"), true);
		enableAccountInterestLog = nonNull(config.getBoolean("enable-interest-log"), true);
		enableBankRevenueLog = nonNull(config.getBoolean("enable-profit-log"), true);
		enableDebugLog = nonNull(config.getBoolean("enable-debug-log"), true);
		cleanupLogDays = nonNull(config.getInt("cleanup-log-days"), 30);
        enableWorldGuardIntegration = nonNull(config.getBoolean("enable-worldguard-integration"), true);
        enableGriefPreventionIntegration = nonNull(config.getBoolean("enable-griefprevention-integration"), true);
		enableWorldEditIntegration = nonNull(config.getBoolean("enable-worldedit-integration"), true);
        removeAccountOnError = nonNull(config.getBoolean("remove-account-on-error"), true);
        blacklist = nonNull(config.getStringList("blacklist"), Collections::emptyList);
		bankRevenueFunction = nonNull(config.getString("bank-revenue-function"), "(0.10 * x) * (1 - g) * ln(n)");
		wgAllowCreateBankDefault = nonNull(config.getBoolean("worldguard-default-flag-value"), false);
		disabledWorlds = nonNull(config.getStringList("disabled-worlds"), Collections::emptyList);
		enableMail = nonNull(config.getBoolean("enable-mail"), true);
		languageFile = nonNull(config.getString("language-file"), "en_US");
		nameRegex = nonNull(config.getString("name-regex"), "");

		if (firstLoad || langReload)
			loadLanguageConfig(showMessages);
		if (!firstLoad && langReload)
			LangUtils.reload();
    }

    public static class ConfigPair<K> extends Pair<Boolean, K> {
		private ConfigPair(Boolean b, K k) {
			super(nonNull(b, true), k);
		}
		public boolean isOverridable() { return super.getFirst(); }
		public K getDefault() { return super.getSecond(); }
	}

	private static <T> T nonNull(T ifNotNull, T ifNull) {
		return nonNull(ifNotNull, () -> ifNull);
	}

	private static <T> T nonNull(T ifNotNull, Supplier<T> ifNull) {
		return Utils.nonNull(ifNotNull, ifNull);
	}

	private static LocalTime convertToLocalTime(String string) {
		try {
			return LocalTime.parse(string);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private static ItemStack convertToItemStack(String string) {
		try {
			return new ItemStack(Objects.requireNonNull(Material.getMaterial(string)));
		} catch (NullPointerException e) {
			return null;
		}
	}

	private void loadLanguageConfig(boolean showMessages) {
		langConfig = new LangConfig(plugin, showMessages);
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
				langConfig.load(specifiedLangFile);
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
					langConfig.load(defaultLangFile);
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
						langConfig.loadFromString(br.lines().collect(Collectors.joining("\n")));
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
