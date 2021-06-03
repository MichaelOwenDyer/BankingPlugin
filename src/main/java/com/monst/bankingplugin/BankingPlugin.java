package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.account.AccountCommand;
import com.monst.bankingplugin.commands.bank.BankCommand;
import com.monst.bankingplugin.commands.control.ControlCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.LanguageConfig;
import com.monst.bankingplugin.events.account.AccountInitializedEvent;
import com.monst.bankingplugin.events.bank.BankInitializedEvent;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.listeners.*;
import com.monst.bankingplugin.repository.AccountRepository;
import com.monst.bankingplugin.repository.BankRepository;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.sql.SQLite;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.UpdateChecker.Result;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BankingPlugin extends JavaPlugin {

	private static BankingPlugin instance;

	private Config config;
	private LanguageConfig languageConfig;

	private AccountCommand accountCommand;
	private BankCommand bankCommand;
	private ControlCommand controlCommand;

	private AccountRepository accountRepository;
	private BankRepository bankRepository;

	private InterestEventScheduler scheduler;

	private boolean isUpdateNeeded = false;
	private String latestVersion = "";
	private String downloadLink = "";
	private PrintWriter debugWriter;

	private Economy economy;
	private Essentials essentials;
	private Database database;

	private Plugin worldGuard;
	private GriefPrevention griefPrevention;
	private WorldEditPlugin worldEdit;

	public final String[] STARTUP_MESSAGE = new String[] {
			ChatColor.GREEN + "   __ " + ChatColor.DARK_GREEN + "  __",
			ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |__)   " + ChatColor.DARK_GREEN + "BankingPlugin" + ChatColor.AQUA + " v" + getDescription().getVersion(),
			ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |   " + ChatColor.DARK_GRAY + "        by monst",
			""
	};

	/**
	 * @return an instance of BankingPlugin
	 */
	public static BankingPlugin getInstance() {
		return instance;
	}

	@Override
    public void onLoad() {
        instance = this;

        config = new Config(this);
        languageConfig = new LanguageConfig(this);

        if (Config.enableDebugLog.get()) {
            try {
				Path debugLogFile = getDataFolder().toPath().resolve("debug.txt");
                debugWriter = new PrintWriter(Files.newOutputStream(debugLogFile), true);
			} catch (IOException e) {
                getLogger().info("Failed to instantiate FileWriter.");
                e.printStackTrace();
            }
        }

        debugf("Loading BankingPlugin version %s", getDescription().getVersion());

        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
			Optional<IWrappedFlag<WrappedState>> createBankFlag = WorldGuardWrapper.getInstance()
					.registerFlag("create-bank", WrappedState.class,
							Config.worldGuardDefaultFlagValue.get() ? WrappedState.ALLOW : WrappedState.DENY);

			debug("Flag create-bank: " + createBankFlag.isPresent());
        }
    }

	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);

		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			debug("Could not find plugin \"Vault\".");
			getLogger().severe("Could not find plugin \"Vault\".");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!setupEconomy()) {
			debug("Could not find plugin \"Essentials\".");
			getLogger().severe("Could not find plugin \"Essentials\".");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

		switch (Utils.getServerVersion()) {
			case "v1_15_R1":
			case "v1_15_R2":
			case "v1_16_R1":
			case "v1_16_R2":
			case "v1_16_R3":
				break;
			default:
				debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
				getLogger().warning("Server version not officially supported: " + Utils.getServerVersion() + "!");
				getLogger().warning("Plugin may still work, but more errors are expected!");
		}

		initializeRepositories();
		getLanguageConfig().reload();
		loadExternalPlugins();
		initializeScheduler();
		initializeCommands();
		// checkForUpdates();
		initializeDatabase();
        registerListeners();
        registerExternalListeners();
		loadBanksAndAccounts();
		enableMetrics();

	}

	@Override
	public void onDisable() {
		debug("Disabling BankingPlugin...");

		ClickType.clear();

		if (bankRepository != null)
			for (Bank bank : bankRepository.getAll())
				bankRepository.remove(bank, false);

		if (database != null) {
			if (database instanceof SQLite)
				((SQLite) database).vacuum();
			database.disconnect();
		}

		if (debugWriter != null)
			debugWriter.close();
	}

	/**
     * Set up the Vault economy
     * @return whether an economy plugin has been registered with Vault
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
            return false;
		economy = rsp.getProvider();
		return true;
    }

    private void initializeRepositories() {
		accountRepository = new AccountRepository(this);
		bankRepository = new BankRepository(this);
	}

    private void initializeCommands() {
		accountCommand = new AccountCommand(this);
		bankCommand = new BankCommand(this);
		controlCommand = new ControlCommand(this);
	}

	/**
	 * Find other plugins running on the server that BankingPlugin can integrate with.
	 */
	private void loadExternalPlugins() {

        Plugin griefPreventionPlugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
		if (griefPreventionPlugin instanceof GriefPrevention)
            griefPrevention = (GriefPrevention) griefPreventionPlugin;

		Plugin worldEditPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if (worldEditPlugin instanceof WorldEditPlugin)
			worldEdit = (WorldEditPlugin) worldEditPlugin;

		Plugin essentialsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin instanceof Essentials)
			essentials = (Essentials) essentialsPlugin;

		if (hasWorldGuard())
            WorldGuardWrapper.getInstance().registerEvents(this);
    }

    private void initializeScheduler() {
		scheduler = new InterestEventScheduler(this);
	}

    private void enableMetrics() {
		debug("Initializing metrics...");

		Metrics metrics = new Metrics(this, 8474);
		metrics.addCustomChart(new Metrics.AdvancedPie("bank-types", () -> {
			Map<String, Integer> typeFrequency = new HashMap<>();
			int playerBanks = 0;
			int adminBanks = 0;

			for (Bank bank : bankRepository.getAll())
				if (bank.isPlayerBank())
					playerBanks++;
				else
					adminBanks++;

			typeFrequency.put("Admin", adminBanks);
			typeFrequency.put("Player", playerBanks);

			return typeFrequency;
		}));
		metrics.addCustomChart(new Metrics.SimplePie("account-info-item",
				() -> Optional.ofNullable(Config.accountInfoItem.get()).map(m -> m.getType().toString()).orElse("none")
		));
		metrics.addCustomChart(new Metrics.SimplePie("self-banking",
				() -> Config.allowSelfBanking.get() ? "Enabled" : "Disabled"));
	}

	/**
	 * Initialize the {@link Database}
	 * @see SQLite
	 */
	private void initializeDatabase() {
		database = new SQLite();
		debug("Database initialized.");
	}

	// URLs NOT YET SET UP
    // DO NOT USE
	@SuppressWarnings("unused")
	private void checkForUpdates() {
        if (!Config.enableUpdateChecker.get()) {
            return;
        }

        runTaskAsynchronously(() -> {
			UpdateChecker uc = new UpdateChecker(BankingPlugin.this);
			Result result = uc.check();

			switch (result) {
				case TRUE:
					latestVersion = uc.getVersion();
					downloadLink = uc.getLink();
					isUpdateNeeded = true;

					getLogger().warning(String.format("Version %s is available! You are running version %s.",
							latestVersion, getDescription().getVersion()));
					break;

				case FALSE:
					latestVersion = "";
					downloadLink = "";
					isUpdateNeeded = false;
					break;

				case ERROR:
					latestVersion = "";
					downloadLink = "";
					isUpdateNeeded = false;
					getLogger().severe("An error occurred while checking for updates.");
					break;
			}
        });
    }

	/**
	 * Register all listeners necessary for BankingPlugin to function.
	 * @see AccountBalanceListener
	 * @see AccountInteractListener
	 * @see AccountProtectListener
	 * @see ChestTamperingListener
	 * @see InterestEventListener
	 * @see NotifyPlayerOnJoinListener
	 */
	private void registerListeners() {
    	debug("Registering listeners...");
		getServer().getPluginManager().registerEvents(new AccountBalanceListener(this), this);
    	getServer().getPluginManager().registerEvents(new AccountInteractListener(this), this);
		getServer().getPluginManager().registerEvents(new AccountProtectListener(this), this);
    	getServer().getPluginManager().registerEvents(new ChestTamperingListener(this), this);
    	getServer().getPluginManager().registerEvents(new InterestEventListener(this), this);
		getServer().getPluginManager().registerEvents(new NotifyPlayerOnJoinListener(this), this);
		getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this); // Third-party GUI listener
	}

	/**
	 * Register listeners specific to external plugins that may or may not be enabled.
	 * @see GriefPreventionListener
	 * @see WorldGuardListener
	 */
	private void registerExternalListeners() {
		if (hasGriefPrevention())
			getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
		if (hasWorldGuard())
			getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);
    }

	/**
	 * Initializes all banks and accounts stored in the {@link Database}.
	 */
	private void loadBanksAndAccounts() {
		reload(true,
                Callback.of(result -> {
                	Set<Bank> banks = result.getBanks();
					Set<Account> accounts = result.getAccounts();

					new BankInitializedEvent(banks).fire();
					new AccountInitializedEvent(accounts).fire();

					String message = String.format("Initialized %d bank%s and %d account%s.",
							banks.size(), banks.size() == 1 ? "" : "s",
							accounts.size(), accounts.size() == 1 ? "" : "s");

					getLogger().info(message);
					debug(message);

					scheduler.scheduleAll();
				})
		);
	}

	/**
	 * Reload the plugin
	 *  @param showConsoleMessages Whether messages about the language file should be
	 *                            shown in the console
	 * @param callback            Callback that - if succeeded - returns the amount
	 *                            of accounts that were reloaded (as {@code int})
	 */
	public void reload(boolean showConsoleMessages, Callback<ReloadResult> callback) {
		debug("Loading banks and accounts from database...");

		getDatabase().connect(Callback.of(
				result -> {
					Collection<Bank> banksBeforeReload = bankRepository.getAll();
					Collection<Account> accountsBeforeReload = accountRepository.getAll();

					Set<Bank> reloadedBanks = new HashSet<>();
					Set<Account> reloadedAccounts = new HashSet<>();

					for (Bank bank : banksBeforeReload) {
						bankRepository.remove(bank, false);
						debugf("Removed bank (#%d)", bank.getID());
					}

					getDatabase().getBanksAndAccounts(showConsoleMessages, Callback.of(
							bankAccountsMap -> {
								bankAccountsMap.forEach((bank, bankAccounts) -> {
									bankRepository.add(bank, false);
									reloadedBanks.add(bank);
									for (Account account : bankAccounts) {
										if (account.create(showConsoleMessages)) {
											accountRepository.add(account, false, account.callUpdateChestName());
											reloadedAccounts.add(account);
										} else
											debug("Could not re-create account from database! (#" + account.getID() + ")");
									}
								});

								if (!banksBeforeReload.isEmpty() && banksBeforeReload.size() != reloadedBanks.size())
									debugf("Number of banks before load was %d and is now %d.",
											banksBeforeReload.size(), reloadedBanks.size());

								if (!accountsBeforeReload.isEmpty() && accountsBeforeReload.size() != reloadedAccounts.size())
									debugf("Number of accounts before load was %d and is now %d.",
											accountsBeforeReload.size(), reloadedAccounts.size());

								Callback.yield(callback, new ReloadResult(reloadedBanks, reloadedAccounts));
							},
							error -> Callback.error(callback, error)
					));
				},
				error -> {
					Callback.error(callback, error);
					// Database connection probably failed => disable plugin to prevent more errors
					getLogger().severe("No database access! Disabling BankingPlugin.");
					if (error != null)
						getLogger().severe(error.getMessage());
					getServer().getPluginManager().disablePlugin(BankingPlugin.this);
				}
		));
	}

	public static class ReloadResult extends Pair<Set<Bank>, Set<Account>> {
		public ReloadResult(Set<Bank> banks, Set<Account> accounts) {
			super(banks, accounts);
		}
		public Set<Bank> getBanks() { return super.getFirst(); }
		public Set<Account> getAccounts() { return super.getSecond(); }
	}

	/**
	 * Prints a message to the <i>/plugins/BankingPlugin/debug.txt</i> file.
	 * @param message the message to be printed
	 */
	public void debug(String message) {
		if (!Config.enableDebugLog.get() || debugWriter == null)
			return;

		String timestamp = Utils.formatTime(Calendar.getInstance().getTime());
		debugWriter.printf("[%s] %s%n", timestamp, message);

		if (debugWriter.checkError())
			getLogger().severe("Failed to print debug message.");
	}

	/**
	 * Prints a message with special formatting to the debug file.
	 */
	public void debugf(String message, Object... format) {
		debug(String.format(message, format));
	}

	/**
	 * Prints a {@link Throwable}'s stacktrace to the
	 * <i>/plugins/BankingPlugin/debug.txt</i> file
	 *
	 * @param throwable the {@link Throwable} of which the stacktrace will be printed
	 */
	public void debug(Throwable throwable) {
		if (!Config.enableDebugLog.get() || debugWriter == null)
			return;
		throwable.printStackTrace(debugWriter);
		debugWriter.flush();
	}

	/**
	 * @return BankingPlugin's {@link Database}
	 */
	public Database getDatabase() {
		return database;
	}

	/**
	 * @return the instance of {@link AccountRepository}
	 */
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

	/**
	 * @return the instance of {@link BankRepository}
	 */
	public BankRepository getBankRepository() {
		return bankRepository;
	}

	/**
	 * @return the instance of {@link InterestEventScheduler}
	 */
	public InterestEventScheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return BankingPlugin's {@link Config}
	 */
	public Config getPluginConfig() {
		return config;
	}

	public LanguageConfig getLanguageConfig() {
		return languageConfig;
	}

	/**
	 * @return the {@link Economy} registered by Vault
	 */
	public Economy getEconomy() {
		return economy;
	}

	/**
	 * @return the instance of {@link Essentials} the plugin is integrated with, if it exists
	 */
	public Essentials getEssentials() {
		return essentials;
	}

	/**
	 * @return whether the plugin is integrated with {@link com.sk89q.worldedit.WorldEdit}
	 */
	public boolean hasWorldEdit() {
		return worldEdit != null && worldEdit.isEnabled();
	}

	/**
	 * @return the instance of {@link com.sk89q.worldedit.WorldEdit} the plugin is integrated with, if it exists
	 */
	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}

	/**
	 * @return whether the plugin is integrated with WorldGuard
	 */
	public boolean hasWorldGuard() {
		return worldGuard != null && worldGuard.isEnabled();
	}

	/**
	 * @return whether the plugin is integrated with {@link GriefPrevention}
	 */
	public boolean hasGriefPrevention() {
		return griefPrevention != null && griefPrevention.isEnabled();
	}

	/**
	 * @return the instance of {@link GriefPrevention} the plugin is integrated with, if it exists
	 */
	public GriefPrevention getGriefPrevention() {
		return griefPrevention;
	}

	/**
	 * @return the instance of {@link AccountCommand}
	 */
	public AccountCommand getAccountCommand() {
		return accountCommand;
	}

	/**
	 * @return the instance of {@link BankCommand}
	 */
	public BankCommand getBankCommand() {
		return bankCommand;
	}

	/**
	 * @return the instance of {@link ControlCommand}
	 */
	public ControlCommand getControlCommand() {
		return controlCommand;
	}

	public Reader getTextResourceMirror(String file) {
		return super.getTextResource(file);
	}

	public static BukkitTask runTask(Runnable runnable) {
		return Utils.bukkitRunnable(runnable).runTask(getInstance());
	}

	public static BukkitTask runTaskLater(Runnable runnable, long delay) {
		return Utils.bukkitRunnable(runnable).runTaskLater(getInstance(), delay);
	}

	public static BukkitTask runTaskAsynchronously(Runnable runnable) {
		return Utils.bukkitRunnable(runnable).runTaskAsynchronously(getInstance());
	}

}
