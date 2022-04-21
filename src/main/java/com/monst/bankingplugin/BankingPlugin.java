package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.google.gson.*;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.command.account.AccountCommand;
import com.monst.bankingplugin.command.bank.BankCommand;
import com.monst.bankingplugin.command.plugin.BPCommand;
import com.monst.bankingplugin.configuration.Configuration;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.listener.*;
import com.monst.bankingplugin.persistence.PersistenceManager;
import com.monst.bankingplugin.persistence.service.*;
import com.monst.bankingplugin.util.*;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BankingPlugin extends JavaPlugin {

    /*	Configuration  */
    private Configuration configuration;

    /*	Entity Utils  */
    private PersistenceManager persistenceManager;

    /*  Services  */
    private SchedulerService schedulerService;
    private PaymentService paymentService;

    /*	Hard Dependencies  */
    private Economy economy;
    private Essentials essentials;

    /*	Soft Dependencies  */
    private Plugin worldGuard;
    private GriefPrevention griefPrevention;
    private WorldEditPlugin worldEdit;

    /*	Update Package  */
    private UpdatePackage updatePackage;

    /*	Debug  */
    private PrintWriter debugWriter;

    /*  Startup Message	 */
    private final String[] STARTUP_MESSAGE = new String[] {
            new ColorStringBuilder().green("   __ ").darkGreen("  __ ").toString(),
            new ColorStringBuilder().green("  |__)").darkGreen(" |__)").darkGreen("   BankingPlugin").aqua(" v").append(getDescription().getVersion()).toString(),
            new ColorStringBuilder().green("  |__)").darkGreen(" |   ").darkGray( "        by monst").toString(),
            "",
    };

    @Override
    public void onLoad() {
        configuration = new Configuration(this);
        config().languageFile.loadTranslations();
        debugf("Loading BankingPlugin version %s", getDescription().getVersion());
        registerWorldGuardFlag();
    }

    @Override
    public void onEnable() {
        if (config().enableStartupMessage.get())
            printStartupMessage();

        if (!checkForDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        checkServerVersion();
        loadExternalPlugins();

        persistenceManager = new PersistenceManager(this);
        schedulerService = new SchedulerService(this);
        schedulerService.scheduleAll();
        paymentService = new PaymentService(this);

        new AccountCommand(this).register();
        new BankCommand(this).register();
        new BPCommand(this).register();

        if (config().enableStartupUpdateCheck.get())
		    checkForUpdates();

        registerListeners();
        enableMetrics();
    }

    @Override
    public void onDisable() {
        debug("Disabling BankingPlugin...");

        ClickAction.clear();
        SubCommand.clearCache();

        if (debugWriter != null)
            debugWriter.close();
        if (persistenceManager != null)
            persistenceManager.shutdown();
        if (schedulerService != null)
            schedulerService.unscheduleAll();
    }

    private void registerWorldGuardFlag() {
        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            Optional<IWrappedFlag<WrappedState>> createBankFlag = WorldGuardWrapper.getInstance()
                    .registerFlag("create-bank", WrappedState.class, config().worldGuardDefaultFlagValue.get());
            debug("WorldGuard flag present: " + createBankFlag.isPresent());
        }
    }

    public void printStartupMessage() {
        Bukkit.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);
    }

    private boolean checkForDependencies() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null || !vault.isEnabled()) {
            getLogger().severe("Could not find dependency 'Vault'!");
            return false;
        }

        Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (essentials == null || rsp == null || !essentials.isEnabled() || !(essentials instanceof Essentials)) {
            getLogger().severe("Could not find dependency 'Essentials'!");
            return false;
        }
        this.economy = rsp.getProvider();
        this.essentials = (Essentials) essentials;
        return true;
    }

    private void checkServerVersion() {
        List<String> testedVersions = Arrays.asList(
                "v1_13_R2",
                "v1_14_R1", "v1_14_R2", "v1_14_R3", "v1_14_R4",
                "v1_15_R1", "v1_15_R2",
                "v1_16_R1", "v1_16_R2", "v1_16_R3",
                "v1_17_R1", "v1_17_R2",
                "v1_18_R1", "v1_18_R2"
        );
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (!testedVersions.contains(serverVersion)) {
            debugf("Server version not officially supported: %s!", serverVersion);
            getLogger().warning("Server version not officially supported: " + serverVersion + "!");
            getLogger().warning("Plugin may still work, but more errors are expected!");
        }
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

    private void checkForUpdates() {
        checkForUpdates(Callback.of(this,
                updatePackage -> {
                    if (updatePackage == null)
                        return;
                    getLogger().warning("Version " + updatePackage.getVersion() + " of BankingPlugin is available!");
                    if (config().downloadUpdatesAutomatically.get()) {
                        getLogger().info("Downloading update...");
                        updatePackage.download(Callback.of(this,
                                state -> {
                                    switch (state) {
                                        case VALIDATING:
                                            getLogger().info("Validating download...");
                                            break;
                                        case COMPLETED:
                                            getLogger().info("Download complete! Restart the server to apply the update.");
                                            break;
                                    }
                                },
                                error -> getLogger().severe("Auto-update failed. Please run /bp update manually.")
                        ));
                    }
                },
                error -> getLogger().warning("Failed to check for updates!")
        ));
    }

    /**
     * Checks if an update is needed
     *
     * @param callback callback that will return {@code null} if no update is needed,
     *                 or an {@link UpdatePackage} if an update is needed
     */
    public void checkForUpdates(Callback<UpdatePackage> callback) {
        debug("Checking for updates...");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            JsonElement response;
            try {
                URL url = new URL("https://api.curseforge.com/servermods/files?projectIds=31043");
                URLConnection con = url.openConnection();
                con.setConnectTimeout(5000);
                con.setRequestProperty("User-Agent", "BankingPlugin");
                con.setDoOutput(true);

                response = new JsonParser().parse(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } catch (JsonIOException | JsonSyntaxException | IOException e) {
                callback.callSyncError("Failed to query CurseForge API.", e);
                return;
            }

            JsonArray versions;
            try {
                versions = response.getAsJsonArray();
            } catch (IllegalStateException e) {
                callback.callSyncError("Received element was not a Json array. Response: " + response, e);
                return;
            }

            if (versions.size() == 0) {
                // No versions available
                callback.callSyncResult(null);
                return;
            }

            JsonObject update = null;
            for (int i = versions.size() - 1; i >= 0; i--) {
                JsonObject version = versions.get(i).getAsJsonObject();
                String fileName = version.get("name").getAsString();
                debug("Checking version " + fileName);

                if (fileName.compareTo(getDescription().getVersion()) <= 0) {
                    // This version is no newer than current version
                    // No need to check further
                    callback.callSyncResult(null);
                    return;
                }

                if (config().ignoreUpdatesContaining.ignore(fileName)) { // This version is ignored
                    debug("Skipping version " + fileName + " because it contains an ignored tag.");
                    continue;
                }

                // Found the latest non-ignored version available, and it is newer than the current version
                update = version;
                break;
            }

            if (update == null) {
                // No suitable update found
                callback.callSyncResult(null);
                return;
            }

            String fileName = update.get("name").getAsString();
            debug("Found latest version: " + fileName);

            // Create a new update package
            // If an update package already exists, replace it if the found version is even newer
            if (updatePackage == null || fileName.compareTo(updatePackage.getVersion()) > 0) {
                debug("Creating new update package.");
                if (updatePackage != null)
                    updatePackage.setOutdated();
                updatePackage = new UpdatePackage(this, update);
            }

            callback.callSyncResult(updatePackage); // Return the update package, replaced or not
        });
    }

    public UpdatePackage getUpdatePackage() {
        return updatePackage;
    }

    /**
     * Register all listeners necessary for BankingPlugin to function.
     * @see AccountBalanceListener
     * @see AccountInteractListener
     * @see AccountProtectListener
     * @see InterestEventListener
     * @see NotifyPlayerOnJoinListener
     */
    private void registerListeners() {
        debug("Registering listeners...");
        getServer().getPluginManager().registerEvents(new AccountBalanceListener(this), this);
        getServer().getPluginManager().registerEvents(new AccountInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new AccountProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new InterestEventListener(this), this);
        getServer().getPluginManager().registerEvents(new NotifyPlayerOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this); // Third-party GUI listener

        if (griefPrevention != null && griefPrevention.isEnabled())
            getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
        if (worldGuard != null && worldGuard.isEnabled())
            getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        debug("Reloading...");
        reloadPluginConfig();
        reloadPersistenceManager();
    }

    public void reloadPluginConfig() {
        debug("Reloading plugin configuration...");
        saveDefaultConfig();
        reloadConfig();
        config().reload();
        saveConfig();
    }

    public void reloadPersistenceManager() {
        debug("Reloading persistence manager...");
        persistenceManager.reload();
    }

    private void enableMetrics() {
        debug("Initializing metrics...");

        Metrics metrics = new Metrics(this, 8474);
        metrics.addCustomChart(new AdvancedPie("bank_types", () -> {
            Map<String, Integer> bankTypes = new HashMap<>();
            bankTypes.put("Player", getBankService().findPlayerBanks().size());
            bankTypes.put("Admin", getBankService().findAdminBanks().size());
            return bankTypes;
        }));
        metrics.addCustomChart(new SimplePie("account_info_item",
                () -> config().accountInfoItem.get().map(String::valueOf).orElse("none")
        ));
        metrics.addCustomChart(new SimplePie("self_banking", config().allowSelfBanking::toString));
        metrics.addCustomChart(new SimplePie("language_file", config().languageFile::toString));
    }

    /**
     * Prints a message to the <i>/plugins/BankingPlugin/debug.txt</i> file.
     * @param message the message to be printed
     */
    public void debug(String message) {
        if (!config().enableDebugLog.get())
            return;

        if (debugWriter == null)
            instantiateDebugWriter();

        debugWriter.printf("[%s] %s%n", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString(), message);

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
        if (!config().enableDebugLog.get())
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

    /**
     * @return whether the plugin is integrated with WorldEdit
     */
    public boolean isWorldEditIntegrated() {
        return worldEdit != null && worldEdit.isEnabled() && config().enableWorldEditIntegration.get();
    }

    /**
     * @return whether the plugin is integrated with WorldGuard
     */
    public boolean isWorldGuardIntegrated() {
        return worldGuard != null && worldGuard.isEnabled() && config().enableWorldGuardIntegration.get();
    }

    /**
     * @return whether the plugin is integrated with {@link GriefPrevention}
     */
    public boolean isGriefPreventionIntegrated() {
        return griefPrevention != null && griefPrevention.isEnabled() && config().enableGriefPreventionIntegration.get();
    }

    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
    }

    public AccountService getAccountService() {
        return persistenceManager.getAccountService();
    }

    public BankService getBankService() {
        return persistenceManager.getBankService();
    }

    public LastSeenService getLastSeenService() {
        return persistenceManager.getLastSeenService();
    }

    public AccountInterestService getAccountInterestService() {
        return persistenceManager.getAccountInterestService();
    }

    public AccountTransactionService getAccountTransactionService() {
        return persistenceManager.getAccountTransactionService();
    }

    public BankIncomeService getBankIncomeService() {
        return persistenceManager.getBankIncomeService();
    }

    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public Configuration config() {
        return configuration;
    }

    public Path getJarFile() {
        return getFile().toPath();
    }

}
