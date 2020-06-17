package com.monst.bankingplugin.config;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.monst.bankingplugin.BankingPlugin;

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
    public static List<Double> interestPayoutTimes;
    
    /**
     * The baseline interest rate for all accounts.
     **/
	public static Entry<Double, Boolean> interestRate;
    
    /**
     * Whether to enable or disable interest multipliers.
     **/
    public static boolean enableInterestMultipliers;

    /**
     * The list of interest multipliers in sequential order.
     **/
	public static Entry<List<Integer>, Boolean> interestMultipliers;

    /**
	 * The number of interest payout events a player has to own their account for
	 * before they start collecting interest
	 **/
	public static Entry<Integer, Boolean> initialInterestDelay;

	/**
	 * Whether to decrement the interest delay period while a player is offline. 
	 * Set this to false to only decrement the delay period when a player is online for
	 * an interest payout event, and not while offline.
	 **/
	public static Entry<Boolean, Boolean> countInterestDelayOffline;

	/**
	 * The number of payouts a player is allowed to collect offline.
	 **/
	public static Entry<Integer, Boolean> allowedOfflinePayouts;

    /**
	 * The number of payouts a player is allowed to be offline for (but not
	 * necessarily collect) before their multiplier is reset.
	 **/
	public static Entry<Integer, Boolean> allowedOfflineBeforeMultiplierReset;

    /**
	 * The behavior of an offline player's multiplier.
	 **/
	public static Entry<Integer, Boolean> offlineMultiplierBehavior;
    
    /**
	 * The behavior of a player's multiplier at a withdrawal event.
	 **/
	public static Entry<Integer, Boolean> withdrawalMultiplierBehavior;
    
    /**
     * The item with which a player can click an account chest to retrieve information.
     **/
    public static ItemStack accountInfoItem;

    /**
     * The price a player has to pay in order to create a bank.
     **/
	public static double creationPriceBank;
    
    /**
     * The price a player has to pay in order to create an account.
     **/
	public static Entry<Double, Boolean> creationPriceAccount;

	/**
	 * The default minimum balance.
	 */
	public static Entry<Double, Boolean> minBalance;

	/**
	 * The fee that must be paid for a balance lower than the minimum.
	 */
	public static Entry<Double, Boolean> lowBalanceFee;

    /**
     * Whether the account creation price should be refunded at removal.
     */
	public static Entry<Boolean, Boolean> reimburseAccountCreation;

	/**
	 * Whether remove requests should be confirmed
	 */
	public static boolean confirmOnRemove;

	/**
	 * Whether to require confirmation (second execution) of removeall commands on
	 * the server.
	 */
	public static boolean confirmOnRemoveAll;

	/**
	 * The delay, in ticks, before a remove command should be executed on the
	 * server. Set to zero for no delay. Defaults to 600 ticks if negative.
	 */
	public static int removeDelay;

	/**
	 * Amount to insure accounts for in case of chests being corrupted.
	 */
	public static long insureAccountsUpTo;

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
     * The default bank ownership limit for players whose limit is not set via a permission.
     **/
	public static int defaultBankLimit;
    
    /**
     * The default account ownership limit for players whose limit is not set via a permission.
     **/
	public static int defaultAccountLimit;

    /**
     * The default value for the custom WorldGuard flag 'create-bank'
     **/
	public static boolean wgAllowCreateBankDefault;
    
    /**
     * The default value for the custom WorldGuard flag 'create-account'
     **/
    public static boolean wgAllowCreateAccountDefault;

    /**
     * The prefix to be used for database tables.
     */
    public static String databaseTablePrefix;

    private BankingPlugin plugin;

    public Config(BankingPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

        reload(true, true);
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
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            plugin.getConfig().set(property, doubleValue);

            plugin.saveConfig();
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            plugin.getConfig().set(property, boolValue);
        } else {
            plugin.getConfig().set(property, value);
        }

        plugin.saveConfig();

        reload(false, false);
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
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.add(doubleValue);

            plugin.saveConfig();
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.add(boolValue);
        } else {
            list.add(value);
        }

        plugin.saveConfig();

        reload(false, false);
    }

    public void remove(String property, String value) {
        @SuppressWarnings("rawtypes")
		List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.remove(intValue);

            plugin.saveConfig();
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.remove(doubleValue);

            plugin.saveConfig();
            reload(false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.remove(boolValue);
        } else {
            list.remove(value);
        }

        plugin.saveConfig();

        reload(false, false);
    }

    /**
     * Reload the configuration values from config.yml
     * @param firstLoad Whether the config values have not been loaded before
     * @param showMessages Whether console (error) messages should be shown
     */
    public void reload(boolean firstLoad, boolean showMessages) {
        plugin.reloadConfig();
        
        FileConfiguration config = plugin.getConfig();

        mainCommandNameBank = config.getString("main-command-names.bank");
        mainCommandNameAccount = config.getString("main-command-names.account");
		mainCommandNameControl = config.getString("main-command-names.control");
		interestPayoutTimes = (config.getDoubleList("interest-payout-times")) != null
				? config.getDoubleList("interest-payout-times").stream().map(t -> (t % 24 + 24) % 24).distinct()
						.sorted().collect(Collectors.toList())
				: new ArrayList<>();

		interestRate = new SimpleEntry<>(config.getDouble("interest-rate.default"),
				config.getBoolean("interest-rate.allow-override"));

		interestMultipliers = new SimpleEntry<>(config.getIntegerList("interest-multipliers.default") != null
				? config.getIntegerList("interest-multipliers")
				: List.of(1), 
				config.getBoolean("interest-multipliers.allow-override"));

		initialInterestDelay = new SimpleEntry<>(config.getInt("initial-interest-delay.default"),
				config.getBoolean("initial-interest-delay.allow-override"));

		countInterestDelayOffline = new SimpleEntry<>(config.getBoolean("count-interest-delay-offline.default"),
				config.getBoolean("count-interest-delay-offline.allow-override"));

		allowedOfflinePayouts = new SimpleEntry<>(config.getInt("allowed-offline-payouts.default"),
				config.getBoolean("allowed-offline-payouts.allow-override"));

		allowedOfflineBeforeMultiplierReset = new SimpleEntry<>(
				config.getInt("allowed-offline-before-multiplier-reset.default"),
				config.getBoolean("allowed-offline-before-multiplier-reset.allow-override"));

		offlineMultiplierBehavior = new SimpleEntry<>(config.getInt("offline-multiplier-behavior.default"),
				config.getBoolean("offline-multiplier-behavior.allow-override"));

		withdrawalMultiplierBehavior = new SimpleEntry<>(config.getInt("withdrawal-multiplier-behavior.default"),
				config.getBoolean("withdrawal-multiplier-behavior.allow-override"));

		accountInfoItem = new ItemStack(Material.getMaterial(config.getString("account-info-item")));
		creationPriceBank = config.getDouble("creation-prices.bank");
		creationPriceAccount = new SimpleEntry<>(config.getDouble("creation-prices.account.default"),
				config.getBoolean("creation-prices.account.allow-override"));

		minBalance = new SimpleEntry<>(config.getDouble("minimum-account-balance.default"),
				config.getBoolean("minimum-account-balance.allow-override"));

		lowBalanceFee = new SimpleEntry<>(config.getDouble("low-balance-fee.default"),
				config.getBoolean("low-balance-fee.allow-override"));

		reimburseAccountCreation = new SimpleEntry<>(config.getBoolean("reimburse-account-creation.default"),
				config.getBoolean("reimburse-account-creation.allow-override"));

		confirmOnRemove = config.getBoolean("confirm-on-remove");
		confirmOnRemoveAll = config.getBoolean("confirm-on-removeall");
		removeDelay = config.getInt("removeall-delay") >= 0 ? config.getInt("removeall-delay") : 300;
		insureAccountsUpTo = config.getLong("insure-accounts-up-to");
        enableUpdateChecker = config.getBoolean("enable-update-checker");
		enableTransactionLog = config.getBoolean("enable-logs.transaction-log");
		enableInterestLog = config.getBoolean("enable-logs.interest-log");
		enableDebugLog = config.getBoolean("enable-logs.debug-log");
		cleanupLogDays = config.getInt("cleanup-log-days");
        enableWorldGuardIntegration = config.getBoolean("enable-worldguard-integration");
        enableGriefPreventionIntegration = config.getBoolean("enable-griefprevention-integration");
		enableWorldEditIntegration = config.getBoolean("enable-worldedit-integration");
        removeAccountOnError = config.getBoolean("remove-account-on-error");
        blacklist = (config.getStringList("blacklist") != null) ? 
				config.getStringList("blacklist") : new ArrayList<>();
		defaultBankLimit = config.getInt("default-limits.bank");
		defaultAccountLimit = config.getInt("default-limits.account");
		wgAllowCreateBankDefault = config.getBoolean("worldguard-default-flag-values.create-bank");
        wgAllowCreateAccountDefault = config.getBoolean("worldguard-default-flag-values.create-account");
		databaseTablePrefix = config.getString("table-prefix");
        
    }

}
