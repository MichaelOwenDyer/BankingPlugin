package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.PlayerCache;
import com.monst.bankingplugin.commands.account.AccountCommand;
import com.monst.bankingplugin.commands.bank.BankCommand;
import com.monst.bankingplugin.commands.control.ControlCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.LanguageConfig;
import com.monst.bankingplugin.events.account.AccountInitializedEvent;
import com.monst.bankingplugin.events.bank.BankInitializedEvent;
import com.monst.bankingplugin.exceptions.notfound.ChestNotFoundException;
import com.monst.bankingplugin.exceptions.notfound.DependencyNotFoundException;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class BankingPlugin extends JavaPlugin {

	/*	Static Instance	 */
	private static BankingPlugin instance;

	/*	Plugin Configuration  */
	private Config config;
	private LanguageConfig languageConfig;

	/*	Entity Utils  */
	private AccountRepository accountRepository;
	private BankRepository bankRepository;
	private InterestEventScheduler scheduler;
	private Database database;

	/*	Hard Dependencies  */
	private Economy economy;
	private Essentials essentials;

	/*	Soft Dependencies  */
	private Plugin worldGuard;
	private GriefPrevention griefPrevention;
	private WorldEditPlugin worldEdit;

	/*	Debug  */
	private PrintWriter debugWriter;

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
		Config.enableStartupMessage.printIfEnabled();

		try {
			checkForDependencies();
		} catch (DependencyNotFoundException e) {
			debug(e);
			getLogger().severe(e.getMessage());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		checkServerVersion();
		initializeRepositories();
		loadExternalPlugins();
		initializeCommands();
//		checkForUpdates();
        registerListeners();
		initializeDatabase();
		enableMetrics();

	}

	@Override
	public void onDisable() {
		debug("Disabling BankingPlugin...");

		ClickType.clear();
		PlayerCache.clear();

		if (scheduler != null)
			scheduler.unscheduleAllInterestEvents();

		if (bankRepository != null)
			for (Bank bank : bankRepository.getAll())
				bankRepository.remove(bank, false, null);

		if (database != null)
			database.disconnect();

		if (debugWriter != null)
			debugWriter.close();
	}

	private void checkForDependencies() throws DependencyNotFoundException {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if (vault == null || !vault.isEnabled())
			throw new DependencyNotFoundException("Vault");
		Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (essentials == null || rsp == null || !essentials.isEnabled() || !(essentials instanceof Essentials))
			throw new DependencyNotFoundException("Essentials");
		this.economy = rsp.getProvider();
		this.essentials = (Essentials) essentials;
	}

	private void checkServerVersion() {
		List<String> testedVersions = Arrays.asList(
				"v1_15_R1",
				"v1_15_R2",
				"v1_16_R1",
				"v1_16_R2",
				"v1_16_R3",
				"v1_17_R1"
		);
		String serverVersion = Utils.getServerVersion();
		if (!testedVersions.contains(serverVersion)) {
			debugf("Server version not officially supported: %s!", serverVersion);
			getLogger().warning("Server version not officially supported: " + serverVersion + "!");
			getLogger().warning("Plugin may still work, but more errors are expected!");
		}
	}

    private void initializeRepositories() {
		accountRepository = new AccountRepository(this);
		bankRepository = new BankRepository(this);
		scheduler = new InterestEventScheduler(this);
	}

    private void initializeCommands() {
		new AccountCommand(this);
		new BankCommand(this);
		new ControlCommand(this);
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

		if (isWorldGuardIntegrated())
            WorldGuardWrapper.getInstance().registerEvents(this);
    }

	// URLs NOT YET SET UP
    // DO NOT USE
	@SuppressWarnings("unused")
	private void checkForUpdates() {
        if (!Config.enableUpdateChecker.get())
            return;
        async(() -> {
			UpdateChecker uc = new UpdateChecker(this);
			Result result = uc.check();

			if (result == Result.TRUE)
				getLogger().warning(Message.UPDATE_AVAILABLE.with(Placeholder.VERSION).as(uc.getVersion()).translate());
			else if (result == Result.ERROR)
				getLogger().severe("An error occurred while checking for updates.");
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
		reloadEntities(Callback.doNothing());
	}

	/**
	 * Reloads the plugin.
	 * @param callback            Callback that returns the reloaded banks and accounts.
	 */
	public void reload(Callback<Map<Bank, Set<Account>>> callback) {
		debug("Reloading...");
		reloadPluginConfig();
		reloadLanguageConfig();
		reloadEntities(callback);
	}

	public void reloadPluginConfig() {
		reloadConfig();
		config.reload();
		saveConfig();
	}

	public void reloadLanguageConfig() {
		languageConfig.reload();
	}

	/**
	 * Fetches all banks and accounts from the {@link Database}.
	 */
	public void reloadEntities(Callback<Map<Bank, Set<Account>>> callback) {
		getDatabase().connect(Callback.of(
				result -> {
					Set<Bank> reloadedBanks = new HashSet<>();
					Set<Account> reloadedAccounts = new HashSet<>();

					for (Bank bank : bankRepository.getAll()) {
						bankRepository.remove(bank, false);
						debugf("Removed bank #%d", bank.getID());
					}

					getDatabase().getBanksAndAccounts(Callback.of(
							bankAccountsMap -> {
								bankAccountsMap.forEach((bank, bankAccounts) -> {
									bankRepository.add(bank, false);
									reloadedBanks.add(bank);
									for (Account account : bankAccounts) {
										try {
											account.getLocation().findChest();
											accountRepository.add(account, false, account.callUpdateChestName());
											reloadedAccounts.add(account);
										} catch (ChestNotFoundException e) {
											debugf("Account #%d could not be located.");
											if (Config.removeAccountOnError.get())
												accountRepository.remove(account, true);
											else
												accountRepository.addMissingAccount(account);
										}
									}
								});

								new BankInitializedEvent(reloadedBanks).fire();
								new AccountInitializedEvent(reloadedAccounts).fire();

								String initializedMessage = String.format("Initialized %d bank%s and %d account%s.",
										reloadedBanks.size(), reloadedBanks.size() == 1 ? "" : "s",
										reloadedAccounts.size(), reloadedAccounts.size() == 1 ? "" : "s");
								getLogger().info(initializedMessage);
								debug(initializedMessage);

								Callback.callResult(callback, new HashMap<>(bankAccountsMap));
							},
							callback::onError
					));
				},
				error -> {
					callback.onError(error);
					getLogger().severe("No database access! Disabling BankingPlugin.");
					getLogger().severe(error.getMessage());
					getServer().getPluginManager().disablePlugin(this);
				}
		));
	}

	private void enableMetrics() {
		debug("Initializing metrics...");

		Metrics metrics = new Metrics(this, 8474);
		metrics.addCustomChart(new AdvancedPie("bank_types",
				() -> bankRepository.getAll().stream().collect(
						Collectors.toMap(bank -> bank.isPlayerBank() ? "Player" : "Admin", bank -> 1, Integer::sum))
		));
		metrics.addCustomChart(new SimplePie("account_info_item",
				() -> Config.accountInfoItem.get().map(String::valueOf).orElse("none")
		));
		metrics.addCustomChart(new SimplePie("self_banking",
				() -> Config.allowSelfBanking.get() ? "Enabled" : "Disabled"
		));
		metrics.addCustomChart(new SimplePie("language_file", Config.languageFile::getFormatted));
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

	/**
	 * @return the plugin {@link AccountRepository}
	 */
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

	/**
	 * @return the plugin {@link BankRepository}
	 */
	public BankRepository getBankRepository() {
		return bankRepository;
	}

	/**
	 * @return the plugin {@link InterestEventScheduler}
	 */
	public InterestEventScheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return the plugin {@link Database}
	 */
	public Database getDatabase() {
		return database;
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

}
