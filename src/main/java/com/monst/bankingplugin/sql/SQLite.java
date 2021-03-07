package com.monst.bankingplugin.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SQLite extends Database {

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

        Path dbFile = plugin.getDataFolder().toPath().resolve("banking.db");

        if (!Files.exists(dbFile)) {
            try {
                Files.createFile(dbFile);
            } catch (IOException ex) {
                plugin.getLogger().severe("Failed to create database file.");
                plugin.debug("Failed to create database file.");
                plugin.debug(ex);
                return null;
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Vacuums the database to reduce file size
     */
    public void vacuum() {
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

                "CountInterestDelayOffline TEXT NOT NULL",
                "ReimburseAccountCreation TEXT NOT NULL",
                "PayOnLowBalance TEXT NOT NULL",
                "InterestRate REAL NOT NULL",
                "AccountCreationPrice REAL NOT NULL",
                "MinimumBalance REAL NOT NULL",
                "LowBalanceFee REAL NOT NULL",
                "InitialInterestDelay INTEGER NOT NULL",
                "AllowedOfflinePayouts INTEGER NOT NULL",
                "AllowedOfflinePayoutsBeforeMultiplierReset INTEGER NOT NULL",
                "OfflineMultiplierDecrement INTEGER NOT NULL",
                "WithdrawalMultiplierDecrement INTEGER NOT NULL",
                "PlayerBankAccountLimit INTEGER NOT NULL",
                "Multipliers TEXT NOT NULL",
                "InterestPayoutTimes TEXT NOT NULL",

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
    			"RemainingOfflinePayoutsUntilReset INTEGER NOT NULL",

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
                "ExecutorUUID TEXT NOT NULL",
                "Amount REAL NOT NULL",
                "NewBalance REAL NOT NULL",
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
                "Amount REAL NOT NULL",
                "Timestamp TEXT NOT NULL",
                "Time INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTableBankRevenue() {
    	return getQueryCreateTable(tableBankRevenue,
                "RevenueID INTEGER PRIMARY KEY AUTOINCREMENT",
                "BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID) ON DELETE CASCADE",
                "Amount REAL NOT NULL",
                "Timestamp TEXT NOT NULL",
                "Time INTEGER NOT NULL"
        );
    }

    @Override
    String getQueryCreateTableLowBalanceFees() {
    	return getQueryCreateTable(tableLowBalanceFees,
                "FeeID INTEGER PRIMARY KEY AUTOINCREMENT",
                "AccountID INTEGER NOT NULL REFERENCES " + tableAccounts + " (AccountID) ON DELETE CASCADE",
                "BankID INTEGER NOT NULL REFERENCES " + tableBanks + " (BankID)",
                "Amount REAL NOT NULL",
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
