package com.monst.bankingplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.commands.AccountCommand;
import com.monst.bankingplugin.commands.BankCommand;
import com.monst.bankingplugin.commands.ControlCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountInitializedEvent;
import com.monst.bankingplugin.events.bank.BankInitializedEvent;
import com.monst.bankingplugin.events.interest.InterestEvent;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardBankingFlag;
import com.monst.bankingplugin.listeners.AccountBalanceListener;
import com.monst.bankingplugin.listeners.AccountInteractListener;
import com.monst.bankingplugin.listeners.AccountTamperingListener;
import com.monst.bankingplugin.listeners.ChestProtectListener;
import com.monst.bankingplugin.listeners.InterestEventListener;
import com.monst.bankingplugin.listeners.PlayerJoinListener;
import com.monst.bankingplugin.listeners.WorldGuardListener;
import com.monst.bankingplugin.sql.Database;
import com.monst.bankingplugin.sql.SQLite;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.UpdateChecker;
import com.monst.bankingplugin.utils.UpdateChecker.UpdateCheckerResult;
import com.monst.bankingplugin.utils.Utils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;

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
	
	private Economy econ = null;
	private Essentials essentials = null;
	private Database database;
	private AccountUtils accountUtils;
	private BankUtils bankUtils;
	private Utils utils;
	
	private Plugin worldGuard;
	private GriefPrevention griefPrevention;
	private WorldEditPlugin worldEdit;
	
	/**
	 * @return An instance of BankingPlugin
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
                if (!debugLogFile.exists()) {
                    debugLogFile.createNewFile();
                }

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
            WorldGuardBankingFlag.register(this);
        }
        
    }

	@Override
	public void onEnable() {
		debug("Enabling BankingPlugin version " + getDescription().getVersion());

		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            debug("Could not find plugin \"Vault\"");
            getLogger().severe("Could not find plugin \"Vault\"");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            debug("Could not find any Vault economy dependency!");
            getLogger().severe("Could not find any Vault economy dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

		switch (Utils.getServerVersion()) {
		case "v1_15_R1":
			break;
		default:
			debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
			debug("Plugin may still work, but more errors are expected!");
			getLogger().warning("Server version not officially supported: " +
			Utils.getServerVersion() + "!");
			getLogger().warning("Plugin may still work, but more errors are expected!");
		}
		
        accountUtils = new AccountUtils(this);
		bankUtils = new BankUtils(this);

		accountCommand = new AccountCommand(this);
		bankCommand = new BankCommand(this);
		controlCommand = new ControlCommand(this);

        loadExternalPlugins();
		// checkForUpdates();
		initDatabase();
        registerListeners();
        registerExternalListeners();
		initializeBanking();
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
     * Sets up the economy of Vault
     * @return Whether an economy plugin has been registered
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    private void loadExternalPlugins() {

        Plugin griefPreventionPlugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
		if (griefPreventionPlugin instanceof GriefPrevention)
            griefPrevention = (GriefPrevention) griefPreventionPlugin;

		Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if (worldEditPlugin instanceof WorldEditPlugin)
			worldEdit = (WorldEditPlugin) worldEditPlugin;
		else {
			debug("Could not find WorldEdit!");
		}

		Plugin essentialsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin instanceof Essentials) {
			essentials = (Essentials) essentialsPlugin;
			debug("Hooked with Essentials.");
		}

		if (hasWorldGuard())
            WorldGuardWrapper.getInstance().registerEvents(this);
    }
    
	private void initDatabase() {
		database = new SQLite(this);
		debug("Database initialized.");
	}

    // URLs NOT YET CHANGED
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
    
    private void registerListeners() {
    	debug("Registering listeners...");
		getServer().getPluginManager().registerEvents(new AccountBalanceListener(this), this);
    	getServer().getPluginManager().registerEvents(new AccountInteractListener(this), this);
    	getServer().getPluginManager().registerEvents(new AccountTamperingListener(this), this);
    	getServer().getPluginManager().registerEvents(new InterestEventListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
		getServer().getPluginManager().registerEvents(new ChestProtectListener(this), this);

		if (hasWorldGuard())
			getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);
	}

	private void registerExternalListeners() {
		if (hasGriefPrevention())
			getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
    	if (hasWorldGuard())
        	getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);
    }

	/**
	 * Initializes banks
	 */
	private void initializeBanking() {
		bankUtils.reload(false, true, new Callback<int[]>(this) {
			@Override
			public void onResult(int[] result) {
				Bukkit.getServer().getPluginManager().callEvent(new BankInitializedEvent(result[0]));
				getLogger().info("Initialized " + result[0] + " banks");
				debug("Initialized " + result[0] + " banks");

				Bukkit.getServer().getPluginManager().callEvent(new AccountInitializedEvent(result[1]));
				getLogger().info("Initialized " + result[1] + " accounts");
				debug("Initialized " + result[1] + " accounts");
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

	public void scheduleInterestPoints() {
		for (Double time : Config.interestPayoutTimes) {
			int id = scheduleRepeatAtTime(time, new Runnable() {
				@Override
				public void run() {
					Bukkit.getServer().getPluginManager().callEvent(new InterestEvent(instance));
				}
			});

			if (id == -1)
				debug("Interest payout scheduling failed! (" + time + ")");
		}
	}

	private int scheduleRepeatAtTime(double time, Runnable task) {
		// 24 hours/day * 60 minutes/hour * 60 seconds/minute *  20 ticks/second = 1728000 ticks/day
		final long ticksInADay = 1728000L;
		
		Calendar cal = Calendar.getInstance();
		long currentTime = cal.getTimeInMillis();
		
		time = (time % 24 + 24) % 24;
		
		int hour = (int) time;
		int minute = (int) ((time - hour) * 60);
		
		if ((hour < cal.get(Calendar.HOUR_OF_DAY))
				|| (hour == cal.get(Calendar.HOUR_OF_DAY) && minute <= cal.get(Calendar.MINUTE)))
			cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		long offset = cal.getTimeInMillis() - currentTime;
		long ticks = offset / 50L;
		
		debug("Scheduling daily interest payout at " + Utils.convertDoubleTime(time, false));
		
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, task, ticks, ticksInADay);
	}

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
	 * @param throwable {@link Throwable} whose stacktrace will be printed
	 */
	public void debug(Throwable throwable) {
		if (Config.enableDebugLog && fw != null) {
			PrintWriter pw = new PrintWriter(fw);
			throwable.printStackTrace(pw);
			pw.flush();
		}
	}

	public Database getDatabase() {
		return database;
	}

	public AccountUtils getAccountUtils() {
		return accountUtils;
	}

	public BankUtils getBankUtils() {
		return bankUtils;
	}

	public Utils getUtils() {
		return utils;
	}

	public Config getPluginConfig() {
		return config;
	}
	
	public Economy getEconomy() {
		return econ;
	}

	public Essentials getEssentials() {
		return essentials;
	}

	public boolean hasWorldEdit() {
		return worldEdit != null && worldEdit.isEnabled();
	}

	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}

	public boolean hasWorldGuard() {
		return worldGuard != null && worldGuard.isEnabled();
	}
	
	public boolean hasGriefPrevention() {
		return griefPrevention != null & griefPrevention.isEnabled();
	}

	public GriefPrevention getGriefPrevention() {
		return griefPrevention;
	}

	public AccountCommand getAccountCommand() {
		return accountCommand;
	}
	
	public BankCommand getBankCommand() {
		return bankCommand;
	}
	
	public ControlCommand getControlCommand() {
		return controlCommand;
	}

}
