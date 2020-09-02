package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	 * The bank revenue multiplier.
	 */
	public static double bankRevenueMultiplier;

	/**
	 * Worlds where banking should be disabled
	 */
	public static List<String> disabledWorlds;

	public static boolean enableMail;

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
    	boolean set = false;
        try {
            plugin.getConfig().set(property, Integer.parseInt(value));
			set = true;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        if (!set)
			try {
				plugin.getConfig().set(property, Double.parseDouble(value));
				set = true;
			} catch (NumberFormatException e) { /* Value not a double */ }

        if (!set)
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				plugin.getConfig().set(property, Boolean.parseBoolean(value));
				set = true;
			}

        if (!set)
        	if (property.equalsIgnoreCase("interest-payout-times.default"))
        		plugin.getConfig().set(property, Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				plugin.getConfig().set(property, value);

		Bukkit.getPluginManager().callEvent(new PluginConfigureEvent(plugin, property, value));
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
    @SuppressWarnings("all")
	public void add(String property, String value) {
		List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);
		boolean added = false;
		try {
			list.add(Integer.parseInt(value));
			added = true;
		} catch (NumberFormatException e) { /* Value not an integer */ }

		if (!added)
			try {
				list.add(Double.parseDouble(value));
				added = true;
			} catch (NumberFormatException e) { /* Value not a double */ }

		if (!added)
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				list.add(Boolean.parseBoolean(value));
				added = true;
			}

		if (!added)
			if (property.equalsIgnoreCase("interest-payout-times.default"))
				list.addAll(Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				list.add(value);

		Bukkit.getPluginManager().callEvent(new PluginConfigureEvent(plugin, property, value));
        plugin.saveConfig();
		reload();
    }

	@SuppressWarnings("all")
    public void remove(String property, String value) {
        @SuppressWarnings("rawtypes")
		List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);
		boolean removed = false;
		try {
			list.remove(Integer.parseInt(value));
			removed = true;
		} catch (NumberFormatException e) { /* Value not an integer */ }

		if (!removed)
			try {
				list.remove(Double.parseDouble(value));
				removed = true;
			} catch (NumberFormatException e) { /* Value not a double */ }

		if (!removed)
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				list.remove(Boolean.parseBoolean(value));
				removed = true;
			}

		if (!removed)
			if (property.equalsIgnoreCase("interest-payout-times.default"))
				list.removeAll(Arrays.stream(value.replace("-","").split(" "))
						.filter(s -> !s.isEmpty())
						.map(s -> Utils.removePunctuation(s, ':'))
						.collect(Collectors.toList()));
			else
				list.remove(value);

		Bukkit.getPluginManager().callEvent(new PluginConfigureEvent(plugin, property, value));
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
		interestPayoutTimes = new ConfigPair<>(config.getBoolean("interest-payout-times.allow-override"),
				config.getStringList("interest-payout-times.default").stream()
						.filter(t -> {
							try {
								LocalTime.parse(t);
								return true;
							} catch (DateTimeParseException e) {return false;}
						})
						.map(LocalTime::parse)
						.distinct()
						.sorted()
						.collect(Collectors.toList()));
		InterestEventScheduler.scheduleAll();

		interestRate = new ConfigPair<>(config.getBoolean("interest-rate.allow-override"),
				Math.abs(config.getDouble("interest-rate.default")));

		multipliers = new ConfigPair<>(config.getBoolean("interest-multipliers.allow-override"),
				config.getIntegerList("interest-multipliers.default").isEmpty()
					? Collections.singletonList(1)
					: config.getIntegerList("interest-multipliers.default"));

		initialInterestDelay = new ConfigPair<>(config.getBoolean("initial-interest-delay.allow-override"),
				Math.abs(config.getInt("initial-interest-delay.default")));

		countInterestDelayOffline = new ConfigPair<>(config.getBoolean("count-interest-delay-offline.allow-override"),
				config.getBoolean("count-interest-delay-offline.default"));

		allowedOfflinePayouts = new ConfigPair<>(config.getBoolean("allowed-offline-payouts.allow-override"),
				Math.abs(config.getInt("allowed-offline-payouts.default")));

		allowedOfflinePayoutsBeforeReset = new ConfigPair<>(
				config.getBoolean("allowed-offline-payouts-before-multiplier-reset.allow-override"),
				Math.abs(config.getInt("allowed-offline-payouts-before-multiplier-reset.default")));

		offlineMultiplierDecrement = new ConfigPair<>(config.getBoolean("offline-multiplier-decrement.allow-override"),
				Math.abs(config.getInt("offline-multiplier-decrement.default")));

		withdrawalMultiplierDecrement = new ConfigPair<>(config.getBoolean("withdrawal-multiplier-decrement.allow-override"),
				Math.abs(config.getInt("withdrawal-multiplier-decrement.default")));

		try {
			accountInfoItem = new ItemStack(Material.getMaterial(config.getString("account-info-item")));
		} catch (Exception e) {
			plugin.debug("Error reading in account info item from config");
			plugin.debug(e);
			accountInfoItem = new ItemStack(Material.STICK);
		}

		bankCreationPricePlayer = Math.abs(config.getDouble("creation-prices.bank.player"));
		bankCreationPriceAdmin = Math.abs(config.getDouble("creation-prices.bank.admin"));

		accountCreationPrice = new ConfigPair<>(config.getBoolean("creation-prices.account.allow-override"),
				Math.abs(config.getDouble("creation-prices.account.default")));

		reimburseBankCreationPlayer = config.getBoolean("reimburse-creation.bank.player");
		reimburseBankCreationAdmin = config.getBoolean("reimburse-creation.bank.admin");

		reimburseAccountCreation = new ConfigPair<>(config.getBoolean("reimburse-creation.account.allow-override"),
				config.getBoolean("reimburse-creation.account.default"));

		minimumBalance = new ConfigPair<>(config.getBoolean("minimum-account-balance.allow-override"),
				Math.abs(config.getDouble("minimum-account-balance.default")));

		lowBalanceFee = new ConfigPair<>(config.getBoolean("low-balance-fee.allow-override"),
				Math.abs(config.getDouble("low-balance-fee.default")));

		payOnLowBalance = new ConfigPair<>(config.getBoolean("pay-interest-on-low-balance.allow-override"),
				config.getBoolean("pay-interest-on-low-balance.default"));

		playerBankAccountLimit = new ConfigPair<>(config.getBoolean("player-bank-account-limit.allow-override"),
				config.getInt("player-bank-account-limit.default"));

		defaultBankLimit = config.getInt("default-limits.bank");
		defaultAccountLimit = config.getInt("default-limits.account");
		minimumBankVolume = Math.max(config.getInt("bank-size-limits.minimum"), 0);
		maximumBankVolume = Math.max(config.getLong("bank-size-limits.maximum"), 0);
		allowSelfBanking = config.getBoolean("allow-self-banking");
		confirmOnRemove = config.getBoolean("confirm-on-remove");
		confirmOnRemoveAll = config.getBoolean("confirm-on-removeall");
		confirmOnTransfer = config.getBoolean("confirm-on-transfer");
		trustOnTransfer = config.getBoolean("trust-on-transfer");
        enableUpdateChecker = config.getBoolean("enable-update-checker");
		enableTransactionLog = config.getBoolean("enable-transaction-log");
		enableInterestLog = config.getBoolean("enable-interest-log");
		enableDebugLog = config.getBoolean("enable-debug-log");
		cleanupLogDays = config.getInt("cleanup-log-days");
        enableWorldGuardIntegration = config.getBoolean("enable-worldguard-integration");
        enableGriefPreventionIntegration = config.getBoolean("enable-griefprevention-integration");
		enableWorldEditIntegration = config.getBoolean("enable-worldedit-integration");
        removeAccountOnError = config.getBoolean("remove-account-on-error");
        blacklist = config.getStringList("blacklist");
		bankRevenueMultiplier = Math.abs(config.getDouble("bank-revenue-multiplier"));
		wgAllowCreateBankDefault = config.getBoolean("worldguard-default-flag-value");
		disabledWorlds = config.getStringList("disabled-worlds");
		enableMail = config.getBoolean("enable-mail");
		nameRegex = config.getString("name-regex");
		databaseTablePrefix = config.getString("table-prefix");
        
    }

    public static class ConfigPair<K> extends Pair<Boolean, K> {
		private ConfigPair(Boolean b, K k) {
			super(b, k);
		}
		public boolean isOverridable() {
			return super.getFirst();
		}
		public K getDefault() {
			return super.getSecond();
		}
	}
}
