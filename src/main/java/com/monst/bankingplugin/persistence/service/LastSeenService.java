package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.LastSeenRepository;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.util.Set;

public class LastSeenService extends Service {
    
    private final LastSeenRepository repo;
    
    public LastSeenService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.repo = new LastSeenRepository();
    }
    
    @Override
    public void createTables() {
        execute(repo::createTable);
    }
    
    public Instant getLastSeenTime(OfflinePlayer player) {
        return query(con -> repo.getLastSeenTime(con, player.getUniqueId())).orElse(null);
    }
    
    public void updateLastSeenTime(OfflinePlayer player) {
        execute(con -> repo.updateLastSeenTime(con, player.getUniqueId()));
    }
    
    public void updateLastSeenTime(Set<OfflinePlayer> players) {
        execute(con -> repo.updateLastSeenTime(con, players));
    }
    
    public void deleteUnused() {
        transact(repo::deleteUnused);
    }
    
}
