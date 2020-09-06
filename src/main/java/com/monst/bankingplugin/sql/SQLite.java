package com.monst.bankingplugin.sql;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Utils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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

        File folder = plugin.getDataFolder();
        File dbFile = new File(folder, "banking.db");

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
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
     * 
     * @param async Whether the call should be executed asynchronously
     */
    public void vacuum(boolean async) {
        BukkitRunnable runnable = Utils.bukkitRunnable(() -> {
            try (Connection con = dataSource.getConnection(); Statement s = con.createStatement()) {
                s.executeUpdate("VACUUM");
                plugin.debug("Vacuumed SQLite database.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to vacuum database.");
                plugin.debug("Failed to vacuum database.");
                plugin.debug(e);
            }
        });
        if (async)
            runnable.runTaskAsynchronously(plugin);
        else
            runnable.runTask(plugin);
    }

    @Override
    String getQueryCreateTableBanks() {
        return "CREATE TABLE IF NOT EXISTS " + tableBanks + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "name TEXT NOT NULL," // Bank name
            + "owner TEXT NOT NULL," // Owner UUID
            + "co_owners TEXT,"
			
            + "selection_type TEXT NOT NULL CHECK (selection_type IN ('CUBOID', 'POLYGONAL')),"
			+ "world TEXT NOT NULL,"
			+ "minY INTEGER," // only used if polygonal
			+ "maxY INTEGER," // only used if polygonal
        	+ "points TEXT NOT NULL," // Contains min/max points if cuboid, all vertices if polygonal
        
        	+ "account_config TEXT NOT NULL)";
        
		// id,name,owner,co_owners,selection_type,world,maxY,minY,points,account_config
		// ?,?,?,?,?,?,?,?,?,?
    }
    
    @Override
    String getQueryCreateTableAccounts() {
    	return "CREATE TABLE IF NOT EXISTS " + tableAccounts + " ("
    			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
    			+ "bank_id INTEGER NOT NULL," 
				+ "nickname TEXT,"
				+ "owner TEXT NOT NULL," // Owner UUID
				+ "co_owners TEXT,"
    			+ "size INTEGER NOT NULL," // Single or double chest -> 1, 2
    			
				+ "balance TEXT NOT NULL,"
				+ "prev_balance TEXT NOT NULL,"
    			
    			+ "multiplier_stage INTEGER NOT NULL,"
    			+ "remaining_until_payout INTEGER NOT NULL,"
    			+ "remaining_offline_payouts INTEGER NOT NULL,"
    			+ "remaining_offline_until_reset INTEGER NOT NULL,"
    			
				+ "world TEXT NOT NULL,"
    			+ "x INTEGER NOT NULL,"
    			+ "y INTEGER NOT NULL,"
    			+ "z INTEGER NOT NULL)";
    	
		// id,bank_id,nickname,owner,co_owners,size,balance,prev_balance,multiplier_stage,remaining_until_payout,remaining_offline_payouts,remaining_offline_until_reset,world,x,y,z
		// ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?
    }

    @Override
    String getQueryCreateTableTransactionLog() {
        return "CREATE TABLE IF NOT EXISTS " + tableTransactionLog + " ("
        	+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "account_id INTEGER NOT NULL,"
            + "bank_id INTEGER NOT NULL,"
			+ "timestamp TEXT NOT NULL,"
			+ "time INTEGER NOT NULL,"
            
			+ "owner_name TEXT NOT NULL,"
			+ "owner_uuid TEXT NOT NULL,"
			+ "executor_name TEXT,"
			+ "executor_uuid TEXT,"
            
			+ "transaction_type TEXT NOT NULL,"
            + "amount TEXT NOT NULL,"
            + "new_balance TEXT NOT NULL,"
            
			+ "world TEXT NOT NULL,"
            + "x INTEGER NOT NULL,"
            + "y INTEGER NOT NULL,"
            + "z INTEGER NOT NULL)";
        
		// id,account_id,bank_id,timestamp,time,owner_name,owner_uuid,executor_name,executor_uuid,transaction_type,amount,new_balance,world,x,y,z
        // ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?
    }
    
    @Override
    String getQueryCreateTableInterestLog() {
    	return "CREATE TABLE IF NOT EXISTS " + tableInterestLog + " ("
    		+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ "account_id INTEGER NOT NULL,"
    		+ "bank_id INTEGER NOT NULL,"
    		
    		+ "owner_name TEXT NOT NULL,"
			+ "owner_uuid TEXT NOT NULL,"

    		+ "base_amount TEXT NOT NULL,"
    		+ "multiplier INTEGER NOT NULL,"
    		+ "amount TEXT NOT NULL,"

			+ "timestamp TEXT NOT NULL,"
			+ "time INTEGER NOT NULL)";
    	
		// id,account_id,bank_id,owner_name,owner_uuid,base_amount,multiplier,amount,timestamp,time
		// ?,?,?,?,?,?,?,?,?,?
    }

    @Override
    String getQueryCreateTableProfitLog() {
    	return "CREATE TABLE IF NOT EXISTS " + tableBankProfitLog + " ("
    		+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ "bank_id INTEGER NOT NULL,"

    		+ "owner_name TEXT NOT NULL,"
			+ "owner_uuid TEXT NOT NULL,"

    		+ "amount TEXT NOT NULL,"

			+ "timestamp TEXT NOT NULL,"
			+ "time INTEGER NOT NULL)";

		// id,bank_id,owner_name,owner_uuid,amount,timestamp,time
		// ?,?,?,?,?,?,?
    }

    @Override
    String getQueryCreateTableLogout() {
        return "CREATE TABLE IF NOT EXISTS " + tableLogouts + " ("
				+ "player TEXT PRIMARY KEY NOT NULL,"
				+ "time INTEGER NOT NULL)";
    }

    @Override
    String getQueryCreateTableFields() {
        return "CREATE TABLE IF NOT EXISTS " + tableFields + " ("
			+ "field TEXT PRIMARY KEY NOT NULL,"
            + "value INTEGER NOT NULL)";
    }

    @Override
    String getQueryGetTable() {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
    }

}
