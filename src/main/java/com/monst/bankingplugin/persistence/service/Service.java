package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.persistence.ConnectionConsumer;
import com.monst.bankingplugin.persistence.ConnectionFunction;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.util.Promise;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;

public abstract class Service {
    
    final BankingPlugin plugin;
    private final ConnectionSupplier connectionSupplier;
    
    public Service(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        this.plugin = plugin;
        this.connectionSupplier = connectionSupplier;
    }
    
    public abstract void createTables();
    
    // Non-transactional writes
    void execute(ConnectionConsumer writeAction) {
        try (Connection con = connectionSupplier.get()) {
            writeAction.accept(con);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to execute write on database!");
            plugin.debug(e);
        }
    }
    
    // Transactional writes
    void transact(ConnectionConsumer writeAction) {
        try (Connection con = connectionSupplier.get()) {
            try {
                con.setAutoCommit(false);
                writeAction.accept(con);
                con.commit();
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to execute transaction on database!");
            plugin.debug(e);
        }
    }
    
    // Synchronous reads
    <T> Optional<T> query(ConnectionFunction<T> query) {
        try (Connection con = connectionSupplier.get()) {
            return Optional.ofNullable(query.apply(con));
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to execute query on database!");
            plugin.debug(e);
            return Optional.empty();
        }
    }
    
    // Asynchronous reads
    
    <T> Promise<T> async(ConnectionFunction<T> query) {
        return Promise.async(plugin, () -> {
            try (Connection con = connectionSupplier.get()) {
                return query.apply(con);
            }
        });
    }
    
}
