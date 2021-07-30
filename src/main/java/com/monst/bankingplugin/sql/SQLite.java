package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SQLite extends Database {

    public SQLite(BankingPlugin plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        try {
            // Initialize driver class so HikariCP can find it
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to initialize SQLite driver.");
            plugin.debug("Failed to initialize SQLite driver.");
            plugin.debug(e);
            return null;
        }

        Path databaseFolder = plugin.getDataFolder().toPath().resolve("database");
        Path databaseFile = databaseFolder.resolve(Config.databaseFile.get());
        while (!Files.exists(databaseFile)) {
            try {
                Files.createDirectories(databaseFile.getParent());
                Files.createFile(databaseFile);
                plugin.getLogger().info("Created new database at " + databaseFile);
                plugin.debug("Created new database at " + databaseFile);
            } catch (AccessDeniedException | RuntimeException e) {
                plugin.getLogger().severe("Failed to create database file at " + databaseFile + ". Reverting to default directory.");
                plugin.debug("Failed to create database file at " + databaseFile + ". Reverting to default directory.");
                plugin.debug(e);
                databaseFile = databaseFolder.resolve("banking.db");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create database file.");
                plugin.debug("Failed to create database file.");
                plugin.debug(e);
                return null;
            }
        }

        plugin.getLogger().info("Using database \"" + databaseFile.getFileName() + "\"");
        plugin.debug("Using database \"" + databaseFile.getFileName() + "\"");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Vacuums the database to reduce file size
     */
    @Override
    void close() {
        if (query == null)
            return;
        query.update("VACUUM").run();
        plugin.debug("Vacuumed SQLite database.");
    }

    @Override
    String getQueryCreateTable(String tableName, String... attributes) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                Arrays.stream(attributes).collect(Collectors.joining(", ", " (", ")"));
    }

    @Override
    String getQueryCreateTableBanks() {
        return getQueryCreateTable(tableBanks,
                "BankID INTEGER PRIMARY KEY AUTOINCREMENT",
                "Name TEXT NOT NULL UNIQUE",
                "OwnerUUID TEXT",

                "CountInterestDelayOffline TEXT",
                "ReimburseAccountCreation TEXT",
                "PayOnLowBalance TEXT",
                "InterestRate REAL",
                "AccountCreationPrice REAL",
                "MinimumBalance REAL",
                "LowBalanceFee REAL",
                "InitialInterestDelay INTEGER",
                "AllowedOfflinePayouts INTEGER",
                "OfflineMultiplierDecrement INTEGER",
                "WithdrawalMultiplierDecrement INTEGER",
                "PlayerBankAccountLimit INTEGER",
                "Multipliers TEXT",
                "InterestPayoutTimes TEXT",

                "World TEXT NOT NULL",
                "MinX INTEGER NOT NULL",
                "MaxX INTEGER NOT NULL",
                "MinY INTEGER NOT NULL",
                "MaxY INTEGER NOT NULL",
                "MinZ INTEGER NOT NULL",
                "MaxZ INTEGER NOT NULL",
                "PolygonVertices TEXT" // contains list of all vertices if polygonal
        );
    }

    @Override
    String getQueryCreateTableCoOwnsBank() {
        return getQueryCreateTable(tableCoOwnsBank,
                "CoOwnerUUID TEXT",
                "BankID INTEGER REFERENCES " + tableBanks + " (BankID) ON DELETE CASCADE",
                "PRIMARY KEY (CoOwnerUUID, BankID)"
        );
    }

    @Override
    String getQueryCreateTableAccounts() {
    	return getQueryCreateTable(tableAccounts,
    			"AccountID INTEGER PRIMARY KEY AUTOINCREMENT",
    			"BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID) ON DELETE CASCADE",
				"Nickname TEXT NOT NULL",
				"OwnerUUID TEXT NOT NULL",

				"Balance REAL NOT NULL",
				"PreviousBalance REAL NOT NULL",

    			"MultiplierStage INTEGER NOT NULL",
    			"DelayUntilNextPayout INTEGER NOT NULL",
    			"RemainingOfflinePayouts INTEGER NOT NULL",

				"World TEXT NOT NULL",
                "Y INTEGER NOT NULL",
    			"X1 INTEGER NOT NULL",
    			"Z1 INTEGER NOT NULL",
                "X2 INTEGER",
                "Z2 INTEGER"
        );
    }

    @Override
    String getQueryCreateTableCoOwnsAccount() {
        return getQueryCreateTable(tableCoOwnsAccount,
                "CoOwnerUUID TEXT",
                "AccountID INTEGER REFERENCES " + tableAccounts + " (AccountID) ON DELETE CASCADE",
                "PRIMARY KEY (CoOwnerUUID, AccountID)"
        );
    }

    @Override
    String getQueryCreateTableAccountTransactions() {
        return getQueryCreateTable(tableAccountTransactions,
                "TransactionID INTEGER PRIMARY KEY AUTOINCREMENT",
                "AccountID INTEGER NOT NULL REFERENCES " + tableAccounts + " (AccountID) ON DELETE CASCADE",
                "BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID)",
                "ExecutorUUID TEXT NOT NULL",
                "ExecutorName TEXT NOT NULL",
                "NewBalance REAL NOT NULL",
                "PreviousBalance REAL NOT NULL",
                "Amount REAL NOT NULL",
                "Timestamp TEXT NOT NULL",
                "Time INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTableAccountInterest() {
    	return getQueryCreateTable(tableAccountInterest,
                "InterestID INTEGER PRIMARY KEY AUTOINCREMENT",
                "AccountID INTEGER NOT NULL REFERENCES " + tableAccounts + " (AccountID) ON DELETE CASCADE",
                "BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID)",
                "Interest REAL NOT NULL",
                "LowBalanceFee REAL NOT NULL",
                "FinalPayment REAL NOT NULL",
                "Timestamp TEXT NOT NULL",
                "Time INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTableBankIncome() {
    	return getQueryCreateTable(tableBankIncome,
                "IncomeID INTEGER PRIMARY KEY AUTOINCREMENT",
                "BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID) ON DELETE CASCADE",
                "Revenue REAL NOT NULL",
                "Interest REAL NOT NULL",
                "LowBalanceFees REAL NOT NULL",
                "Profit REAL NOT NULL",
                "Timestamp TEXT NOT NULL",
                "Time INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTablePlayers() {
        return getQueryCreateTable(tablePlayers,
                "PlayerUUID TEXT PRIMARY KEY NOT NULL",
                "Name TEXT NOT NULL",
                "LastSeen INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTableFields() {
        return getQueryCreateTable(tableFields,
                "Field TEXT PRIMARY KEY NOT NULL",
                "Value INTEGER NOT NULL"
        );
    }

}
