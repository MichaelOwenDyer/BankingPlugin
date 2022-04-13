package com.monst.bankingplugin.persistence;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.converter.*;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.LastSeen;
import com.monst.bankingplugin.entity.geo.Vector2;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.entity.geo.location.DoubleAccountLocation;
import com.monst.bankingplugin.entity.geo.location.SingleAccountLocation;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.CuboidBankRegion;
import com.monst.bankingplugin.entity.geo.region.PolygonalBankRegion;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.persistence.service.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.persistence.platform.database.HSQLPlatform;
import org.hsqldb.jdbcDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

public class PersistenceManager extends PersistenceProvider {

    static {
        System.setProperty("hsqldb.reconfig_logging", "false");
        Logger.getLogger("hsqldb.db").setLevel(Level.WARNING);
    }

    private final BankingPlugin plugin;
    private EntityManagerFactory emf;

    private final AccountService accountService;
    private final BankService bankService;
    private final LastSeenService lastSeenService;
    private final AccountInterestService accountInterestService;
    private final AccountTransactionService accountTransactionService;
    private final BankIncomeService bankIncomeService;

    public PersistenceManager(BankingPlugin plugin) {
        this.plugin = plugin;
        this.emf = createEntityManagerFactory();
        this.accountService = new AccountService(plugin, this::createEntityManager);
        this.bankService = new BankService(plugin, this::createEntityManager);
        this.lastSeenService = new LastSeenService(plugin, this::createEntityManager);
        this.accountInterestService = new AccountInterestService(plugin, this::createEntityManager);
        this.accountTransactionService = new AccountTransactionService(plugin, this::createEntityManager);
        this.bankIncomeService = new BankIncomeService(plugin, this::createEntityManager);

        cleanupLogs();
    }

    private EntityManagerFactory createEntityManagerFactory() {
        return super.createEntityManagerFactoryImpl(new BankingPluginPersistenceUnitInfo(plugin), new HashMap<>(), true);
    }

    private EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public BankService getBankService() {
        return bankService;
    }

    public LastSeenService getLastSeenService() {
        return lastSeenService;
    }

    public AccountInterestService getAccountInterestService() {
        return accountInterestService;
    }

    public AccountTransactionService getAccountTransactionService() {
        return accountTransactionService;
    }

    public BankIncomeService getBankIncomeService() {
        return bankIncomeService;
    }

    public void shutdown() {
        accountService.resetAllChestTitles();
        {
            EntityManager em = createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery("SHUTDOWN").executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
        emf.close();
    }

    public void reload() {
        shutdown();
        emf = createEntityManagerFactory();
        cleanupLogs();
        accountService.initializeAll();
    }

    private void cleanupLogs() {
        int days = plugin.config().cleanupLogDays.get();
        if (days < 0)
            return;
        final Instant oldest = ZonedDateTime.now().minusDays(days).toInstant();
        lastSeenService.deleteUnused();
        accountInterestService.deleteBefore(oldest);
        accountTransactionService.deleteBefore(oldest);
        bankIncomeService.deleteBefore(oldest);
    }

    private static class BankingPluginPersistenceUnitInfo extends SEPersistenceUnitInfo {

        public BankingPluginPersistenceUnitInfo(BankingPlugin plugin) {
            this.persistenceUnitName = "BankingPlugin";
            this.persistenceProviderClassName = PersistenceProvider.class.getName();
            this.persistenceUnitRootUrl = getRootURL(plugin);
            this.managedClassNames.addAll(createManagedClassNames());
            this.properties = createProperties(plugin);
            this.realClassLoader = plugin.getClass().getClassLoader();
            this.cacheMode = SharedCacheMode.UNSPECIFIED;
            this.validationMode = ValidationMode.NONE;
            this.jarFileUrls = Collections.emptyList();
        }

        private URL getRootURL(BankingPlugin plugin) {
            try {
                return plugin.getJarFile().toUri().toURL();
            } catch (MalformedURLException e) {
                return plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            }
        }

        private List<String> createManagedClassNames() {
            return Stream.of(
                    Account.class,
                    Bank.class,
                    LastSeen.class,
                    FinancialStatement.class,
                    AccountInterest.class,
                    AccountTransaction.class,
                    BankIncome.class,
                    Vector2.class,
                    Vector3.class,
                    AccountLocation.class,
                    SingleAccountLocation.class,
                    DoubleAccountLocation.class,
                    BankRegion.class,
                    CuboidBankRegion.class,
                    PolygonalBankRegion.class,
                    InterestPayoutTimesConverter.class,
                    MultipliersConverter.class,
                    OfflinePlayerConverter.class,
                    VerticesConverter.class,
                    WorldConverter.class
            ).map(Class::getName).collect(Collectors.toList());
        }

        private Properties createProperties(BankingPlugin plugin) {
            Properties properties = new Properties();
            properties.put(TARGET_DATABASE, HSQLPlatform.class.getName()); // Must manually set this due to maven relocation
            properties.put(JDBC_DRIVER, jdbcDriver.class.getName());
            properties.put(JDBC_URL, plugin.config().databaseFile.getJdbcUrl());
            properties.put(JDBC_USER, "user");
            properties.put(JDBC_PASSWORD, "password");
            properties.put(SCHEMA_GENERATION_DATABASE_ACTION, "create");
            properties.put(LOGGING_LEVEL, Level.WARNING.toString());
            return properties;
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return "3.0";
        }
    }

}
