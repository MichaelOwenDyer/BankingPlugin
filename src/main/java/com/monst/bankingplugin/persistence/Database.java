package com.monst.bankingplugin.persistence;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.persistence.service.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZonedDateTime;

public class Database {

    private final BankingPlugin plugin;
    private HikariDataSource dataSource;
    
    private final BankService bankService;
    private final AccountService accountService;
    private final AccountInterestService accountInterestService;
    private final AccountTransactionService accountTransactionService;
    private final BankIncomeService bankIncomeService;
    private final LastSeenService lastSeenService;

    public Database(BankingPlugin plugin) {
        this.plugin = plugin;
        this.dataSource = createDataSource();
        ConnectionSupplier connectionSupplier = this::getConnection;
        this.bankService = new BankService(plugin, connectionSupplier);
        this.accountService = new AccountService(plugin, connectionSupplier);
        this.accountInterestService = new AccountInterestService(plugin, connectionSupplier);
        this.accountTransactionService = new AccountTransactionService(plugin, connectionSupplier);
        this.bankIncomeService = new BankIncomeService(plugin, connectionSupplier);
        this.lastSeenService = new LastSeenService(plugin, connectionSupplier);
        
        accountService.setBankService(bankService);
        bankService.setAccountService(accountService);

        createTables();
        cleanupLogs();
    }
    
    private HikariDataSource createDataSource() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find HSQLDB JDBC Driver!", e);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(plugin.config().databaseFile.getJdbcUrl() + ";hsqldb.sqllog=3");
        config.setConnectionTestQuery("CALL NOW()");
        return new HikariDataSource(config);
    }
    
    private Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new SQLException("DataSource is not initialized");
        return dataSource.getConnection();
    }
    
    public void createTables() {
        bankService.createTables();
        accountService.createTables();
        accountInterestService.createTables();
        accountTransactionService.createTables();
        bankIncomeService.createTables();
        lastSeenService.createTables();
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
    
    public void reload() {
        shutdown();
        dataSource = createDataSource();
        createTables();
        cleanupLogs();
    }

    public void shutdown() {
        if (dataSource == null)
            return;
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("SHUTDOWN");
        } catch (SQLException e) {
            plugin.debug(e);
            plugin.getLogger().severe("Failed to shut down database.");
        }
        dataSource.close();
        dataSource = null;
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
    
}
