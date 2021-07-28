package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
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
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
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

	private LanguageConfig languageConfig;

	private AccountCommand accountCommand;
	private BankCommand bankCommand;
	private ControlCommand controlCommand;

	private AccountRepository accountRepository;
	private BankRepository bankRepository;

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

		saveDefaultConfig();
        languageConfig = new LanguageConfig(this);

        if (Config.enableDebugLog.get())
            instantiateDebugWriter();

        debugf("Loading BankingPlugin version %s", getDescription().getVersion());

        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
			Optional<IWrappedFlag<WrappedState>> createBankFlag = WorldGuardWrapper.getInstance()
					.registerFlag("create-bank", WrappedState.class, Config.worldGuardDefaultFlagValue.getWrappedState());
			debug("WorldGuard flag present: " + createBankFlag.isPresent());
        }
    }

	@Override
	public void onEnable() {
		if (Config.enableStartupMessage.get())
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

		String serverVersion = Utils.getServerVersion();
		switch (serverVersion) {
			case "v1_15_R1":
			case "v1_15_R2":
			case "v1_16_R1":
			case "v1_16_R2":
			case "v1_16_R3":
			case "v1_17_R1":
				break;
			default:
				debug("Server version not officially supported: " + serverVersion + "!");
				getLogger().warning("Server version not officially supported: " + serverVersion + "!");
				getLogger().warning("Plugin may still work, but more errors are expected!");
		}

		initializeRepositories();
		reloadLanguageConfig();
		loadExternalPlugins();
		initializeCommands();
		// checkForUpdates();
        registerListeners();
        registerExternalListeners();
		initializeDatabase();
		initializeBankingEntities();
		enableMetrics();

	}

	@Override
	public void onDisable() {
		debug("Disabling BankingPlugin...");

		ClickType.clear();
		InterestEventScheduler.unscheduleAll();

		if (bankRepository != null)
			for (Bank bank : bankRepository.getAll())
				bankRepository.remove(bank, false, null);

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

	public void reloadLanguageConfig() {
		languageConfig.reload();
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

		if (isWorldGuardIntegrated())
            WorldGuardWrapper.getInstance().registerEvents(this);
    }

	// URLs NOT YET SET UP
    // DO NOT USE
	@SuppressWarnings("unused")
	private void checkForUpdates() {
        if (!Config.enableUpdateChecker.get()) {
            return;
        }

        async(() -> {
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
		if (isGriefPreventionIntegrated())
			getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
		if (isWorldGuardIntegrated())
			getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);
    }

	/**
	 * Initialize the {@link Database}
	 * @see SQLite
	 */
	private void initializeDatabase() {
		database = new SQLite(this);
		debug("Database initialized.");
	}

	private void initializeBankingEntities() {
		fetchBankingEntities(Callback.of(result -> {
			Set<Bank> banks = result.getBanks();
			Set<Account> accounts = result.getAccounts();

			new BankInitializedEvent(banks).fire();
			new AccountInitializedEvent(accounts).fire();

			String message = String.format("Initialized %d bank%s and %d account%s.",
					banks.size(), banks.size() == 1 ? "" : "s",
					accounts.size(), accounts.size() == 1 ? "" : "s");
			getLogger().info(message);
			debug(message);
		}));
	}

	/**
	 * Reloads the plugin.
	 * @param callback            Callback that returns the reloaded banks and accounts.
	 */
	public void reload(Callback<FetchResult> callback) {
		debug("Reloading...");
		Config.reload();
		reloadLanguageConfig();
		fetchBankingEntities(callback);
	}

	/**
	 * Fetches all banks and accounts from the {@link Database}.
	 */
	private void fetchBankingEntities(Callback<FetchResult> callback) {
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

					getDatabase().getBanksAndAccounts(Callback.of(
							bankAccountsMap -> {
								bankAccountsMap.forEach((bank, bankAccounts) -> {
									bankRepository.add(bank, false);
									reloadedBanks.add(bank);
									for (Account account : bankAccounts) {
										if (account.create()) {
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

								InterestEventScheduler.scheduleAllBanks();
								Callback.callResult(callback, new FetchResult(reloadedBanks, reloadedAccounts));
							},
							callback::onError
					));
				},
				error -> {
					callback.onError(error);
					// Database connection probably failed => disable plugin to prevent more errors
					getLogger().severe("No database access! Disabling BankingPlugin.");
					if (error != null)
						getLogger().severe(error.getMessage());
					getServer().getPluginManager().disablePlugin(BankingPlugin.this);
				}
		));
	}

	public static class FetchResult extends Pair<Set<Bank>, Set<Account>> {
		public FetchResult(Set<Bank> banks, Set<Account> accounts) {
			super(banks, accounts);
		}
		public Set<Bank> getBanks() { return super.getFirst(); }
		public Set<Account> getAccounts() { return super.getSecond(); }
	}

	private void enableMetrics() {
		debug("Initializing metrics...");

		Metrics metrics = new Metrics(this, 8474);
		metrics.addCustomChart(new AdvancedPie("bank-types", () -> {
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
		metrics.addCustomChart(new SimplePie("account-info-item",
				() -> Optional.ofNullable(Config.accountInfoItem.get()).map(m -> m.getType().toString()).orElse("none")
		));
		metrics.addCustomChart(new SimplePie("self-banking",
				() -> Config.allowSelfBanking.get() ? "Enabled" : "Disabled"));
	}

	/**
	 * Prints a message to the <i>/plugins/BankingPlugin/debug.txt</i> file.
	 * @param message the message to be printed
	 */
	public void debug(String message) {
		if (!Config.enableDebugLog.get())
			return;

		if (debugWriter == null)
			instantiateDebugWriter();

		String timestamp = Utils.currentTimestamp();
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
		if (!Config.enableDebugLog.get())
			return;

		if (debugWriter == null)
			instantiateDebugWriter();

		throwable.printStackTrace(debugWriter);
		debugWriter.flush();
	}

	private void instantiateDebugWriter() {
		try {
			Path debugLogFile = getDataFolder().toPath().resolve("debug.txt");
			debugWriter = new PrintWriter(Files.newOutputStream(debugLogFile), true);
		} catch (IOException e) {
			getLogger().info("Failed to instantiate FileWriter.");
			e.printStackTrace();
		}
	}

	public void async(Runnable runnable) {
		if (isEnabled())
			Utils.bukkitRunnable(runnable).runTaskAsynchronously(this);
	}

	public void sync(Runnable runnable) {
		if (isEnabled())
			Utils.bukkitRunnable(runnable).runTask(this);
	}

	public void runLater(Runnable runnable, long delay) {
		if (isEnabled())
			Utils.bukkitRunnable(runnable).runTaskLater(this, delay);
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
	public boolean isWorldEditIntegrated() {
		return worldEdit != null && worldEdit.isEnabled() && Config.enableWorldEditIntegration.get();
	}

	/**
	 * @return whether the plugin is integrated with WorldGuard
	 */
	public boolean isWorldGuardIntegrated() {
		return worldGuard != null && worldGuard.isEnabled() && Config.enableWorldGuardIntegration.get();
	}

	/**
	 * @return whether the plugin is integrated with {@link GriefPrevention}
	 */
	public boolean isGriefPreventionIntegrated() {
		return griefPrevention != null && griefPrevention.isEnabled() && Config.enableGriefPreventionIntegration.get();
	}

	/**
	 * @return the instance of {@link com.sk89q.worldedit.WorldEdit} the plugin is integrated with, if it exists
	 */
	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}

	/**
	 * @return the instance of {@link GriefPrevention} the plugin is integrated with, if it exists
	 */
	public GriefPrevention getGriefPrevention() {
		return griefPrevention;
	}

	public Reader getTextResourceMirror(String file) {
		return super.getTextResource(file);
	}

}
