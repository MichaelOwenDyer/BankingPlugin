package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.commands.account.AccountCommand;
import com.monst.bankingplugin.commands.bank.BankCommand;
import com.monst.bankingplugin.commands.control.ControlCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.InterestEvent;
import com.monst.bankingplugin.events.account.AccountInitializedEvent;
import com.monst.bankingplugin.events.bank.BankInitializedEvent;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardBankingFlag;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.listeners.*;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.sql.SQLite;
import com.monst.bankingplugin.utils.*;
import com.monst.bankingplugin.utils.UpdateChecker.UpdateCheckerResult;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

public class BankingPlugin extends JavaPlugin {

	private static BankingPlugin instance;

	private Config config;

	private AccountCommand accountCommand;
	private BankCommand bankCommand;
	private ControlCommand controlCommand;
	
	private boolean isUpdateNeeded = false;
	private String latestVersion = "";
	private String downloadLink = "";
	private FileWriter fw;
	
	private Economy econ;
	private Essentials essentials;
	private Database database;
	private AccountUtils accountUtils;
	private BankUtils bankUtils;
	
	private Plugin worldGuard;
	private GriefPrevention griefPrevention;
	private WorldEditPlugin worldEdit;
	
	private static final Map<LocalTime, Integer> payoutTimeIds = new HashMap<>();

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
            File debugLogFile = new File(getDataFolder(), "debug.txt");

            try {
				if (!debugLogFile.exists())
                    debugLogFile.createNewFile();

                new PrintWriter(debugLogFile).close();

                fw = new FileWriter(debugLogFile, true);
            } catch (IOException e) {
                getLogger().info("Failed to instantiate FileWriter");
                e.printStackTrace();
            }
        }

        debug("Loading BankingPlugin version " + getDescription().getVersion());

        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
			WorldGuardBankingFlag.register(this); // Throws class error with worldedit:World.class
        }
    }

	@Override
	public void onEnable() {
		debug("Enabling BankingPlugin version " + getDescription().getVersion());
		Bukkit.getConsoleSender().sendMessage(Utils.getVersionMessage());

		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			debug("Could not find plugin \"Vault\"");
			getLogger().severe("Could not find plugin \"Vault\"");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!setupEconomy()) {
			debug("Could not find plugin \"Essentials\"");
			getLogger().severe("Could not find plugin \"Essentials\"");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

		switch (Utils.getServerVersion()) {
		case "v1_15_R1": case "v1_16_R1":
			break;
		default:
			debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
			getLogger().warning("Server version not officially supported: " + Utils.getServerVersion() + "!");
			getLogger().warning("Plugin may still work, but more errors are expected!");
		}
		
        accountUtils = new AccountUtils(this);
		bankUtils = new BankUtils(this);

		loadExternalPlugins();

		accountCommand = new AccountCommand(this);
		bankCommand = new BankCommand(this);
		controlCommand = new ControlCommand(this);

		// checkForUpdates();
		// enableMetrics();
		initDatabase();
        registerListeners();
        registerExternalListeners();
		initializeBanksAndAccounts();
		scheduleInterestPoints();
        
	}

	@Override
	public void onDisable() {
		debug("Disabling BankingPlugin...");

		if (accountUtils == null) {
			// Plugin has not been fully enabled (probably due to errors),
			// so only close file writer.
			if (fw != null && Config.enableDebugLog) {
				try {
					fw.close();
				} catch (IOException e) {
					getLogger().severe("Failed to close FileWriter");
					e.printStackTrace();
				}
			}
			return;
		}

		ClickType.clear();

		for (Account account : accountUtils.getAccountsCopy()) {
			accountUtils.removeAccount(account, false);
			debug("Removed account (#" + account.getID() + ")");
		}

		for (Bank bank : bankUtils.getBanksCopy()) {
			bankUtils.removeBank(bank, false);
			debug("Removed bank (\"" + bank.getName() + "\" (#" + bank.getID() + "))");
		}

		if (database != null) {
			((SQLite) database).vacuum(false);
			database.disconnect();
		}

		if (fw != null && Config.enableDebugLog) {
			try {
				fw.close();
			} catch (IOException e) {
				getLogger().severe("Failed to close FileWriter");
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
		// debug(rsp.getProvider().toString());

		econ = rsp.getProvider();
		return econ != null;
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

		Metrics metrics = new Metrics(this, -1);
		metrics.addCustomChart(new Metrics.AdvancedPie("bank-types", () -> {
			Map<String, Integer> typeFrequency = new HashMap<>();
			int playerBanks = 0;
			int adminBanks = 0;

			for (Bank bank : bankUtils.getBanks())
				if (bank.getType() == Bank.BankType.PLAYER)
					playerBanks++;
				else if (bank.getType() == Bank.BankType.ADMIN)
					adminBanks++;

			typeFrequency.put("Admin", adminBanks);
			typeFrequency.put("Player", playerBanks);

			return typeFrequency;
		}));
	}

	/**
	 * Initialize the {@link Database}
	 * @see SQLite
	 */
	private void initDatabase() {
		database = new SQLite(this);
		debug("Database initialized.");
	}

	// URLs NOT YET SET UP
    // DO NOT USE
	private void checkForUpdates() {
        if (!Config.enableUpdateChecker) {
            return;
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                UpdateChecker uc = new UpdateChecker(BankingPlugin.this);
                UpdateCheckerResult result = uc.check();

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
            }
        }.runTaskAsynchronously(this);
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
	private void initializeBanksAndAccounts() {
		bankUtils.reload(false, true,
                new Callback<AbstractMap.SimpleEntry<Collection<Bank>, Collection<Account>>>(this) {
			@Override
			public void onResult(AbstractMap.SimpleEntry<Collection<Bank>, Collection<Account>> result) {
			    Collection<Bank> banks = result.getKey();
                Collection<Account> accounts = result.getValue();

				Bukkit.getServer().getPluginManager().callEvent(new BankInitializedEvent(banks));
                Bukkit.getServer().getPluginManager().callEvent(new AccountInitializedEvent(accounts));

				String message = String.format("Initialized %s bank%s and %s account%s.",
                        banks.size(), banks.size() == 1 ? "" : "s",
                        accounts.size(), accounts.size() == 1 ? "" : "s");

                getLogger().info(message);
                debug(message);
			}

			@Override
			public void onError(Throwable throwable) {
				// Database connection probably failed => disable plugin to prevent more errors
				getLogger().severe("No database access. Disabling BankingPlugin");
				if (throwable != null)
					getLogger().severe(throwable.getMessage());

				getServer().getPluginManager().disablePlugin(BankingPlugin.this);
			}
		});
	}

	/**
	 * Create Bukkit tasks to trigger interest events at the times specified in the {@link Config}
	 * @see #scheduleRepeatAtTime(LocalTime)
	 * @see InterestEvent
	 * @see InterestEventListener
	 */
	public void scheduleInterestPoints() {
		for (LocalTime time : payoutTimeIds.keySet())
			if (!Config.interestPayoutTimes.contains(time)) {
				Bukkit.getScheduler().cancelTask(payoutTimeIds.get(time));
				debug("Removed interest payout at " + time);
			}
		for (LocalTime time : Config.interestPayoutTimes) {
			if (time != null && !payoutTimeIds.containsKey(time)) {
				int id = scheduleRepeatAtTime(time);
				if (id == -1)
					debug("Interest payout scheduling failed! (" + time + ")");
				else {
					payoutTimeIds.put(time, id);
					debug("Scheduled interest payment at " + time);
				}
			}
		}
	}

	/**
	 * Perform the necessary arithmetic to schedule a {@link LocalTime} from the {@link Config}
	 * as a {@link org.bukkit.scheduler.BukkitTask} repeating every 24 hours.
	 * @param time the time to be scheduled
	 * @return the ID of the scheduled task, or -1 if the task was not scheduled
	 */
	private int scheduleRepeatAtTime(LocalTime time) {
		// 24 hours/day * 60 minutes/hour * 60 seconds/minute *  20 ticks/second = 1728000 ticks/day
		final long ticksInADay = 1728000L;
		
		Calendar cal = Calendar.getInstance();
		long currentTime = cal.getTimeInMillis();
		
		if (LocalTime.now().isAfter(time))
			cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, time.getHour());
		cal.set(Calendar.MINUTE, time.getMinute());
		cal.set(Calendar.SECOND, time.getSecond());
		cal.set(Calendar.MILLISECOND, 0);
		
		long offset = cal.getTimeInMillis() - currentTime;
		long ticks = offset / 50L;
		
		debug("Scheduling daily interest payout at " + time.toString());
		
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
				() -> Bukkit.getServer().getPluginManager().callEvent(new InterestEvent(this)), ticks, ticksInADay);
	}

	/**
	 * Print a message to the <i>/plugins/BankingPlugin/debug.txt</i> file.
	 * @param message the message to be printed
	 */
	public void debug(String message) {
		if (Config.enableDebugLog && fw != null) {
			try {
				Calendar c = Calendar.getInstance();
				String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(c.getTime());
				fw.write(String.format("[%s] %s\r\n", timestamp, message));
				fw.flush();
			} catch (IOException e) {
				getLogger().severe("Failed to print debug message.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Print a {@link Throwable}'s stacktrace to the
	 * <i>/plugins/BankingPlugin/debug.txt</i> file
	 * 
	 * @param throwable the {@link Throwable} of which the stacktrace will be printed
	 */
	public void debug(Throwable throwable) {
		if (Config.enableDebugLog && fw != null) {
			PrintWriter pw = new PrintWriter(fw);
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
	 * @return the instance of {@link AccountUtils}
	 */
	public AccountUtils getAccountUtils() {
		return accountUtils;
	}

	/**
	 * @return the instance of {@link BankUtils}
	 */
	public BankUtils getBankUtils() {
		return bankUtils;
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
		return econ;
	}

	/**
	 * @return whether the plugin is integrated with {@link Essentials}
	 */
	public boolean hasEssentials() {
		return essentials != null && essentials.isEnabled();
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

}
