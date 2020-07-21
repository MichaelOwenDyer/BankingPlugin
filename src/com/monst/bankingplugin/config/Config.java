package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class Config {

    /**
     * The bank command of BankingPlugin <i>(default: bank)</i>
     **/
    public static String mainCommandNameBank;

    /**
     * The account command of BankingPlugin <i>(default: account)</i>
     **/
    public static String mainCommandNameAccount;
    
    /**
     * The control command of BankingPlugin <i>(default: bankingplugin)</i>
     **/
    public static String mainCommandNameControl;

    /**
     * The real-life times for account interest payouts.
     **/
	public static List<LocalTime> interestPayoutTimes;
    
    /**
     * The baseline interest rate for all accounts.
     **/
	public static Entry<Boolean, Double> interestRate;
    
    /**
     * The list of interest multipliers in sequential order.
     **/
	public static Entry<Boolean, List<Integer>> interestMultipliers;

    /**
	 * The number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static Entry<Boolean, Integer> initialInterestDelay;

	/**
	 * Whether to decrement the interest delay period while a player is offline. 
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static Entry<Boolean, Boolean> countInterestDelayOffline;

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static Entry<Boolean, Integer> allowedOfflinePayouts;

    /**
	 * The number of payouts a player is allowed to be offline for (but not
	 * necessarily collect) before their multiplier is reset.
	 **/
	public static Entry<Boolean, Integer> allowedOfflineBeforeMultiplierReset;

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static Entry<Boolean, Integer> offlineMultiplierBehavior;
    
    /**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static Entry<Boolean, Integer> withdrawalMultiplierBehavior;
    
    /**
     * The item with which a player can click an account chest to retrieve information.
     **/
    public static ItemStack accountInfoItem;

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static Entry<Double, Double> creationPriceBank;
    
    /**
     * The price a player has to pay in order to create an account.
     **/
	public static Entry<Boolean, Double> creationPriceAccount;

	/**
	 * The default minimum balance.
	 */
	public static Entry<Boolean, Double> minBalance;

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static Entry<Boolean, Double> lowBalanceFee;

	/**
	 * Whether the bank creation price should be refunded at removal.
	 */
	public static Entry<Boolean, Boolean> reimburseBankCreation;

    /**
     * Whether the account creation price should be refunded at removal.
     */
	public static Entry<Boolean, Boolean> reimburseAccountCreation;

	/**
	 * The default account limit per player per bank.
	 */
	public static Entry<Boolean, Integer> playerBankAccountLimit;

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
	public static int defaultBankVolumeLimit;
	
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
     * <p>Whether the update checker should run on start and notify players on join.</p>
     * The command is not affected by this setting and will continue to check for updates.
     **/
    public static boolean enableUpdateChecker;

    /**
     * Whether deposits and withdrawals should be logged in the database.
     **/
	public static boolean enableTransactionLog;

	/**
	 * Whether interest payouts should be logged in the database.
	 **/
	public static boolean enableInterestLog;

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
     * Whether WorldGuard integration should be enabled.
     **/
    public static boolean enableWorldGuardIntegration;

    /**
     * Whether GriefPrevention integration should be enabled.
     **/
    public static boolean enableGriefPreventionIntegration;

	/**
	 * Whether WorldEdit integration should be enabled.
	 **/
	public static boolean enableWorldEditIntegration;

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
	 * The bank revenue multiplier.
	 */
	public static double bankRevenueMultiplier;

    /**
     * The default value for the custom WorldGuard flag 'create-bank'
     **/
	public static boolean wgAllowCreateBankDefault;

	/**
	 * The regex pattern that bank names and account nicknames should be matched
	 * against.
	 */
	public static String nameRegex;
    
    /**
     * The prefix to be used for database tables.
     */
    public static String databaseTablePrefix;

	private final BankingPlugin plugin;

    public Config(BankingPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

		reload();
    }

    /**
     * <p>Set a configuration value</p>
     * <i>Config is automatically reloaded</i>
     *
     * @param property Property to change
     * @param value    Value to set
     */
    public void set(String property, String value) {
        try {
            int intValue = Integer.parseInt(value);
            plugin.getConfig().set(property, intValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            plugin.getConfig().set(property, doubleValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            plugin.getConfig().set(property, boolValue);
        } else {
            plugin.getConfig().set(property, value);
        }

        plugin.saveConfig();

		reload();
    }

    /**
     * Add a value to a list in the config.yml.
     * If the list does not exist, a new list with the given value will be created
     *
     * @param property Location of the list
     * @param value    Value to add
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void add(String property, String value) {
		List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.add(intValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.add(doubleValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.add(boolValue);
        } else {
            list.add(value);
        }

        plugin.saveConfig();

		reload();
    }

    public void remove(String property, String value) {
        @SuppressWarnings("rawtypes")
		List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.remove(intValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.remove(doubleValue);

            plugin.saveConfig();
			reload();

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.remove(boolValue);
        } else {
            list.remove(value);
        }

        plugin.saveConfig();

		reload();
    }

    /**
     * Reload the configuration values from config.yml
     */
	public void reload() {
        plugin.reloadConfig();
        
        FileConfiguration config = plugin.getConfig();

        mainCommandNameBank = config.getString("main-command-names.bank");
        mainCommandNameAccount = config.getString("main-command-names.account");
		mainCommandNameControl = config.getString("main-command-names.control");
		interestPayoutTimes = config.getStringList("interest-payout-times").stream()
		.map(t -> t.replace(".", ":"))
		.map(t -> t.contains(":") ? t : t + ":00")
		.map(t -> t.substring(0, t.indexOf(':')).length() > 1 ? t : "0" + t)
		.map(t -> {
			try {
				return LocalTime.parse(t);
			} catch (DateTimeParseException e) {
				plugin.debug("Could not parse time from config: " + t);
				return null;
			}
		}).collect(Collectors.toList());
		if (plugin.isEnabled())
			plugin.scheduleInterestPoints();

		interestRate = new SimpleEntry<>(config.getBoolean("interest-rate.allow-override"),
				config.getDouble("interest-rate.default"));

		interestMultipliers = new SimpleEntry<>(config.getBoolean("interest-multipliers.allow-override"),
				config.getIntegerList("interest-multipliers.default").isEmpty()
				? Collections.singletonList(1)
				: config.getIntegerList("interest-multipliers.default"));

		initialInterestDelay = new SimpleEntry<>(config.getBoolean("initial-interest-delay.allow-override"),
				config.getInt("initial-interest-delay.default"));

		countInterestDelayOffline = new SimpleEntry<>(config.getBoolean("count-interest-delay-offline.allow-override"),
				config.getBoolean("count-interest-delay-offline.default"));

		allowedOfflinePayouts = new SimpleEntry<>(config.getBoolean("allowed-offline-payouts.allow-override"),
				config.getInt("allowed-offline-payouts.default"));

		allowedOfflineBeforeMultiplierReset = new SimpleEntry<>(
				config.getBoolean("allowed-offline-before-multiplier-reset.allow-override"),
				config.getInt("allowed-offline-before-multiplier-reset.default"));

		offlineMultiplierBehavior = new SimpleEntry<>(config.getBoolean("offline-multiplier-behavior.allow-override"),
				config.getInt("offline-multiplier-behavior.default"));

		withdrawalMultiplierBehavior = new SimpleEntry<>(config.getBoolean("withdrawal-multiplier-behavior.allow-override"),
				config.getInt("withdrawal-multiplier-behavior.default"));

		accountInfoItem = new ItemStack(Objects.requireNonNull(Material.getMaterial(config.getString("account-info-item"))));

		creationPriceBank = new SimpleEntry<>(config.getDouble("creation-prices.bank.admin"),
				config.getDouble("creation-prices.bank.player"));

		creationPriceAccount = new SimpleEntry<>(config.getBoolean("creation-prices.account.allow-override"),
				config.getDouble("creation-prices.account.default"));

		minBalance = new SimpleEntry<>(config.getBoolean("minimum-account-balance.allow-override"),
				config.getDouble("minimum-account-balance.default"));

		lowBalanceFee = new SimpleEntry<>(config.getBoolean("low-balance-fee.allow-override"),
				config.getDouble("low-balance-fee.default"));

		reimburseBankCreation = new SimpleEntry<>(config.getBoolean("reimburse-creation.bank.admin"),
				config.getBoolean("reimburse-creation.bank.player"));

		reimburseAccountCreation = new SimpleEntry<>(config.getBoolean("reimburse-account-creation.allow-override"),
				config.getBoolean("reimburse-account-creation.default"));

		playerBankAccountLimit = new SimpleEntry<>(config.getBoolean("player-bank-account-limit.allow-override"),
				config.getInt("player-bank-account-limit.default"));

		defaultBankLimit = config.getInt("default-limits.bank");
		defaultAccountLimit = config.getInt("default-limits.account");
		minimumBankVolume = config.getInt("bank-size-limits.minimum");
		defaultBankVolumeLimit = config.getInt("bank-size-limits.maximum");
		allowSelfBanking = config.getBoolean("allow-self-banking");
		confirmOnRemove = config.getBoolean("confirm-on-remove");
		confirmOnRemoveAll = config.getBoolean("confirm-on-removeall");
		confirmOnTransfer = config.getBoolean("confirm-on-transfer");
		trustOnTransfer = config.getBoolean("trust-on-transfer");
        enableUpdateChecker = config.getBoolean("enable-update-checker");
		enableTransactionLog = config.getBoolean("enable-logs.transaction-log");
		enableInterestLog = config.getBoolean("enable-logs.interest-log");
		enableDebugLog = config.getBoolean("enable-logs.debug-log");
		cleanupLogDays = config.getInt("cleanup-log-days");
        enableWorldGuardIntegration = config.getBoolean("enable-worldguard-integration");
        enableGriefPreventionIntegration = config.getBoolean("enable-griefprevention-integration");
		enableWorldEditIntegration = config.getBoolean("enable-worldedit-integration");
        removeAccountOnError = config.getBoolean("remove-account-on-error");
        blacklist = config.getStringList("blacklist");
		bankRevenueMultiplier = config.getDouble("bank-revenue-multiplier");
		wgAllowCreateBankDefault = config.getBoolean("worldguard-default-flag-value.create-bank");
		nameRegex = config.getString("name-regex");
		databaseTablePrefix = config.getString("table-prefix");
        
    }

}
