package com.monst.bankingplugin;

import com.earth2me.essentials.Essentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.command.account.AccountCommand;
import com.monst.bankingplugin.command.bank.BankCommand;
import com.monst.bankingplugin.command.plugin.BPCommand;
import com.monst.bankingplugin.configuration.Configuration;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.exception.MissingDependencyException;
import com.monst.bankingplugin.external.GriefPreventionListener;
import com.monst.bankingplugin.external.WorldGuardListener;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.listener.*;
import com.monst.bankingplugin.persistence.Database;
import com.monst.bankingplugin.persistence.service.*;
import com.monst.bankingplugin.util.*;
import com.sk89q.worldedit.WorldEdit;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BankingPlugin extends JavaPlugin {

    /*	Configuration  */
    private Configuration configuration;

    /*	Database  */
    private Database database;

    /*  Services  */
    private SchedulerService schedulerService;
    private PaymentService paymentService;
    private Worths worths;

    /*	Hard Dependencies  */
    private Economy economy;

    /*	Soft Dependencies  */
    private Plugin worldGuard;
    private GriefPrevention griefPrevention;
    private WorldEditPlugin worldEdit;

    /*	Update Package  */
    private Update update;

    /*	Debug  */
    private Logger logger = Logger.NO_OP; // Default to no-op logger, will be replaced by a real logger if debug is enabled

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
        debug("Loading BankingPlugin version %s", getDescription().getVersion());
        registerWorldGuardFlag();
    }

    @Override
    public void onEnable() {
        if (config().enableStartupMessage.get())
            printStartupMessage();
        
        try {
            economy = findEconomy();
            worths = new Worths(this, findEssentials());
        } catch (MissingDependencyException e) {
            getLogger().severe(e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        checkServerVersion();
        loadSoftDependencies();

        database = new Database(this);
        
        // TODO: Necessary? Maybe lazy load?
        for (Account account : getAccountService().findAll()) {
            account.setBalance(worths.appraise(account));
            account.updateChestTitle();
        }
        
        schedulerService = new SchedulerService(this);
        paymentService = new PaymentService(this);

        new AccountCommand(this);
        new BankCommand(this);
        new BPCommand(this);

        if (config().enableStartupUpdateCheck.get()) {
            checkForUpdates().then(update -> {
                if (update == null)
                    return;
                getLogger().warning("Version " + update.getVersion() + " of BankingPlugin is available!");
                if (config().downloadUpdatesAutomatically.get())
                    update.download();
            }).catchError(error -> getLogger().warning("Failed to check for updates!"));
        }

        registerListeners();
        enableMetrics();
    }

    @Override
    public void onDisable() {
        debug("Disabling BankingPlugin...");
        
        for (Account account : getAccountService().findAll())
            account.resetChestTitle();

        ClickAction.clear();
        SubCommand.clearCache();

        setDebugLogEnabled(false);
        if (database != null)
            database.shutdown();
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
    
    private Economy findEconomy() throws MissingDependencyException {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null || !vault.isEnabled())
            throw new MissingDependencyException("Could not find dependency 'Vault'!");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            throw new MissingDependencyException("Could not find Vault economy provider!");
        return rsp.getProvider();
    }
    
    private Essentials findEssentials() throws MissingDependencyException {
        Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null || !essentials.isEnabled() || !(essentials instanceof Essentials))
            throw new MissingDependencyException("Could not find dependency 'Essentials'!");
        return (Essentials) essentials;
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
            debug("Server version not officially supported: %s!", serverVersion);
            getLogger().warning("Server version not officially supported: " + serverVersion + "!");
            getLogger().warning("Plugin may still work, but more errors are expected!");
        }
    }

    /**
     * Find other plugins running on the server that BankingPlugin can integrate with.
     */
    private void loadSoftDependencies() {
        Plugin griefPreventionPlugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (griefPreventionPlugin instanceof GriefPrevention)
            griefPrevention = (GriefPrevention) griefPreventionPlugin;

        Plugin worldEditPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin instanceof WorldEditPlugin)
            worldEdit = (WorldEditPlugin) worldEditPlugin;

        if (isWorldGuardIntegrated())
            WorldGuardWrapper.getInstance().registerEvents(this);
    }

    /**
     * Checks if an update is needed.
     */
    public Promise<Update> checkForUpdates() {
        debug("Checking for updates...");
        return Promise.async(this, () -> {
            JsonElement response;
            URL url = new URL("https://api.github.com/repos/FreshLlamanade/BankingPlugin/releases");
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestProperty("User-Agent", "BankingPlugin");
            con.setDoOutput(true);

            response = new JsonParser().parse(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

            // Releases are sorted by date, newest first
            JsonArray releases = response.getAsJsonArray();

            if (releases.size() == 0)
                // No versions available
                return update;

            String versionNumber = null;
            JsonObject jar = null;
            for (int i = releases.size() - 1; i >= 0; i--) {
                JsonObject version = releases.get(i).getAsJsonObject();
                versionNumber = version.get("name").getAsString();
                debug("Checking version " + versionNumber);

                if (versionNumber.compareTo(getDescription().getVersion()) <= 0) {
                    // This version is no newer than current version
                    // No need to check further
                    return update;
                }

                if (config().ignoreUpdatesContaining.ignore(versionNumber)) { // This version is ignored
                    debug("Skipping version " + versionNumber + " because it contains an ignored tag.");
                    continue;
                }
    
                for (JsonElement asset : version.get("assets").getAsJsonArray()) {
                    if (((JsonObject) asset).get("name").getAsString().endsWith(".jar")) {
                        // Found the latest non-ignored jar available, and it is newer than the current version
                        jar = (JsonObject) asset;
                        break;
                    }
                }
            }

            if (jar == null)
                // No suitable update found
                return update;

            debug("Found latest version: " + versionNumber);
            
            // An update already exists newer or equal to this one
            if (update != null && update.getVersion().compareTo(versionNumber) > 0)
                return update;
            
            if (update != null)
                update.setOutdated();

            // Create a new update package
            debug("Creating new update package.");
            URL fileURL;
            fileURL = new URL(jar.get("browser_download_url").getAsString());
            String checksum = Optional.ofNullable(jar.get("md5")).map(JsonElement::getAsString).orElse(null);
            int downloadSize = jar.get("size").getAsInt();
    
            return update = new Update(this, versionNumber, fileURL, checksum, downloadSize);
        });
    }

    public Update getUpdate() {
        return update;
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
        getServer().getPluginManager().registerEvents(new GUIActionListener(), this);

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
        database.reload();
    }

    public void reloadPluginConfig() {
        debug("Reloading plugin configuration...");
        saveDefaultConfig();
        reloadConfig();
        config().reload();
        saveConfig();
    }

    public void reloadDatabase() {
        debug("Reloading database...");
        database.reload();
    }

    private void enableMetrics() {
        debug("Initializing metrics...");

        Metrics metrics = new Metrics(this, 8474);
        metrics.addCustomChart(new AdvancedPie("bank_types", () -> {
            Map<String, Integer> bankTypes = new HashMap<>();
            bankTypes.put("Player", getBankService().countPlayerBanks());
            bankTypes.put("Admin", getBankService().countAdminBanks());
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
        logger.log(message);
    }

    /**
     * Prints a message with special formatting to the debug file.
     */
    public void debug(String message, Object... format) {
        logger.log(String.format(message, format));
    }

    /**
     * Prints a {@link Throwable}'s stacktrace to the
     * <i>/plugins/BankingPlugin/debug.txt</i> file
     *
     * @param throwable the {@link Throwable} of which the stacktrace will be printed
     */
    public void debug(Throwable throwable) {
        logger.log(throwable);
    }
    
    public void setDebugLogEnabled(boolean enabled) {
        logger.close();
        if (enabled) {
            Path debugFile = getDataFolder().toPath().resolve("debug.txt");
            try {
                PrintWriter debugWriter = new PrintWriter(Files.newOutputStream(debugFile), true);
                logger = Logger.printingTo(debugWriter);
                return;
            } catch (IOException e) {
                getLogger().severe("Failed to create debug writer.");
            }
        }
        logger = Logger.NO_OP;
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
    
    public Database getDatabase() {
        return database;
    }
    
    public AccountService getAccountService() {
        return database.getAccountService();
    }

    public BankService getBankService() {
        return database.getBankService();
    }

    public LastSeenService getLastSeenService() {
        return database.getLastSeenService();
    }

    public AccountInterestService getAccountInterestService() {
        return database.getAccountInterestService();
    }

    public AccountTransactionService getAccountTransactionService() {
        return database.getAccountTransactionService();
    }

    public BankIncomeService getBankIncomeService() {
        return database.getBankIncomeService();
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

    public Worths getWorths() {
        return worths;
    }

    public WorldEdit getWorldEdit() {
        return worldEdit.getWorldEdit();
    }

    public Configuration config() {
        return configuration;
    }

    public String getFileName() {
        return getFile().getName();
    }

}
