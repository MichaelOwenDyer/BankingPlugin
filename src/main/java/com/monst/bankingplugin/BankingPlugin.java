package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.account.AccountCommand;
import com.monst.bankingplugin.commands.bank.BankCommand;
import com.monst.bankingplugin.commands.control.ControlCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountInitializedEvent;
import com.monst.bankingplugin.events.bank.BankInitializedEvent;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.listeners.*;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.sql.SQLite;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.UpdateChecker.Result;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankingPlugin extends JavaPlugin {

	private static BankingPlugin instance;

	private Config config;

	private AccountCommand accountCommand;
	private BankCommand bankCommand;
	private ControlCommand controlCommand;

	private AccountRepository accountRepository;
	private BankRepository bankRepository;

	private InterestEventScheduler scheduler;

	private boolean isUpdateNeeded = false;
	private String latestVersion = "";
	private String downloadLink = "";
	private FileWriter debugWriter;

	private Economy economy;
	private Essentials essentials;
	private Database database;

	private Plugin worldGuard;
	private GriefPrevention griefPrevention;
	private WorldEditPlugin worldEdit;

	public final String[] VERSION_MSG = new String[] {
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

        if (Config.enableDebugLog) {
            try {
				File debugLogFile = new File(getDataFolder(), "debug.txt");
				if (!debugLogFile.exists())
					debugLogFile.createNewFile();
                new PrintWriter(debugLogFile).close();
                debugWriter = new FileWriter(debugLogFile, true);
            } catch (IOException e) {
                getLogger().info("Failed to instantiate FileWriter.");
                e.printStackTrace();
            }
        }

        debug("Loading BankingPlugin version " + getDescription().getVersion());

        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
			Optional<IWrappedFlag<WrappedState>> createBankFlag = WorldGuardWrapper.getInstance()
					.registerFlag("create-bank", WrappedState.class,
							Config.wgAllowCreateBankDefault ? WrappedState.ALLOW : WrappedState.DENY);

			debug("Flag create-bank: " + createBankFlag.isPresent());
        }
    }

	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(VERSION_MSG);

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

        accountRepository = new AccountRepository(this);
		bankRepository = new BankRepository(this);

		LangUtils.reload();

		loadExternalPlugins();

		scheduler = new InterestEventScheduler(this);

		accountCommand = new AccountCommand(this);
		bankCommand = new BankCommand(this);
		controlCommand = new ControlCommand(this);

		// checkForUpdates();
		enableMetrics();
		initDatabase();
        registerListeners();
        registerExternalListeners();
		initializeBanking();

	}

	@Override
	public void onDisable() {
		debug("Disabling BankingPlugin...");

		if (accountRepository == null) {
			// Plugin has not been fully enabled (probably due to errors),
			// so only close file writer.
			if (debugWriter != null && Config.enableDebugLog) {
				try {
					debugWriter.close();
				} catch (IOException e) {
					getLogger().severe("Failed to close FileWriter.");
					e.printStackTrace();
				}
			}
			return;
		}

		ClickType.clear();

		bankRepository.getAll().forEach(bank -> {
			bankRepository.remove(bank, false);
			debugf("Removed bank \"%s\" (#%d)", bank.getName(), bank.getID());
		});

		if (database != null) {
			((SQLite) database).vacuum();
			database.disconnect();
		}

		if (debugWriter != null && Config.enableDebugLog) {
			try {
				debugWriter.close();
			} catch (IOException e) {
				getLogger().severe("Failed to close FileWriter.");
				e.printStackTrace();
			}
		}
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

    private void enableMetrics() {
		debug("Initializing Metrics...");

		Metrics metrics = new Metrics(this, 8474);
		metrics.addCustomChart(new Metrics.AdvancedPie("bank-types", () -> {
			Map<String, Integer> typeFrequency = new HashMap<>();
			int playerBanks = 0;
			int adminBanks = 0;

			for (Bank bank : bankRepository.getAll())
				if (bank.isPlayerBank())
					playerBanks++;
				else if (bank.isAdminBank())
					adminBanks++;

			typeFrequency.put("Admin", adminBanks);
			typeFrequency.put("Player", playerBanks);

			return typeFrequency;
		}));
		metrics.addCustomChart(new Metrics.SimplePie("account-info-item",
				() -> WordUtils.capitalizeFully(Config.accountInfoItem.getType()
						.name().replace("_", " "))
		));
		metrics.addCustomChart(new Metrics.SimplePie("self-banking",
				() -> Config.allowSelfBanking ? "Enabled" : "Disabled"));
	}

	/**
	 * Initialize the {@link Database}
	 * @see SQLite
	 */
	private void initDatabase() {
		database = new SQLite();
		debug("Database initialized.");
	}

	// URLs NOT YET SET UP
    // DO NOT USE
	@SuppressWarnings("unused")
	private void checkForUpdates() {
        if (!Config.enableUpdateChecker) {
            return;
        }

        Utils.bukkitRunnable(() -> {
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
        }).runTaskAsynchronously(this);
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
	private void initializeBanking() {
		reload(false, true,
                Callback.of(this, result -> {
                	Collection<Bank> banks = result.getBanks();
					Collection<Account> accounts = result.getAccounts();

					Bukkit.getServer().getPluginManager().callEvent(new BankInitializedEvent(banks));
					Bukkit.getServer().getPluginManager().callEvent(new AccountInitializedEvent(accounts));

					String message = String.format("Initialized %d bank%s and %d account%s.",
							banks.size(), banks.size() == 1 ? "" : "s",
							accounts.size(), accounts.size() == 1 ? "" : "s");

					getLogger().info(message);
					debug(message);

					scheduler.scheduleAll();
				}, error -> {
					// Database connection probably failed => disable plugin to prevent more errors
					getLogger().severe("No database access! Disabling BankingPlugin.");
					if (error != null)
						getLogger().severe(error.getMessage());

					getServer().getPluginManager().disablePlugin(BankingPlugin.this);
				})
		);
	}

	/**
	 * Reload the plugin
	 *
	 * @param reloadConfig        Whether the configuration should also be reloaded
	 * @param showConsoleMessages Whether messages about the language file should be
	 *                            shown in the console
	 * @param callback            Callback that - if succeeded - returns the amount
	 *                            of accounts that were reloaded (as {@code int})
	 */
	public void reload(boolean reloadConfig, boolean showConsoleMessages, Callback<ReloadResult> callback) {
		debug("Loading banks and accounts from database...");

		if (reloadConfig)
			getPluginConfig().reload(false, true, true);

		getDatabase().connect(Callback.of(this,
				result -> {
					Collection<Bank> banks = bankRepository.getAll();
					Collection<Account> accounts = accountRepository.getAll();

					Set<Bank> reloadedBanks = new HashSet<>();
					Set<Account> reloadedAccounts = new HashSet<>();

					for (Bank bank : banks) {
						bankRepository.remove(bank, false);
						debugf("Removed bank (#%d)", bank.getID());
					}

					getDatabase().getBanksAndAccounts(showConsoleMessages, Callback.of(this,
							bankAccountsMap -> {
								bankAccountsMap.forEach((bank, bankAccounts) -> {
									bankRepository.add(bank, false);
									reloadedBanks.add(bank);
									for (Account account : bankAccounts) {
										if (account.create(showConsoleMessages)) {
											accountRepository.add(account, false, account.callUpdateName());
											reloadedAccounts.add(account);
										} else
											debug("Could not re-create account from database! (#" + account.getID() + ")");
									}
								});

								if (banks.size() != 0 && banks.size() != reloadedBanks.size())
									debugf("Number of banks before load was %d and is now %d.",
											banks.size(), reloadedBanks.size());
								if (accounts.size() != 0 && accounts.size() != reloadedAccounts.size())
									debugf("Number of accounts before load was %d and is now %d.",
											accounts.size(), reloadedAccounts.size());

								Callback.yield(callback, new ReloadResult(reloadedBanks, reloadedAccounts));
							},
							callback::error
					));
				},
				callback::error
		));
	}

	public static class ReloadResult extends Pair<Collection<Bank>, Collection<Account>> {
		public ReloadResult(Collection<Bank> banks, Collection<Account> accounts) {
			super(banks, accounts);
		}
		public Collection<Bank> getBanks() { return super.getFirst(); }
		public Collection<Account> getAccounts() { return super.getSecond(); }
	}

	/**
	 * Prints a message to the <i>/plugins/BankingPlugin/debug.txt</i> file.
	 * @param message the message to be printed
	 */
	public void debug(String message) {
		if (Config.enableDebugLog && debugWriter != null) {
			try {
				Calendar c = Calendar.getInstance();
				String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(c.getTime());
				debugWriter.write(String.format("[%s] %s%n", timestamp, message));
				debugWriter.flush();
			} catch (IOException e) {
				getLogger().severe("Failed to print debug message.");
				e.printStackTrace();
			}
		}
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
		if (Config.enableDebugLog && debugWriter != null) {
			PrintWriter pw = new PrintWriter(debugWriter);
			throwable.printStackTrace(pw);
			pw.flush();
		}
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

}
