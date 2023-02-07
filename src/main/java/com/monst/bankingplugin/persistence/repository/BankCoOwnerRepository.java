package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.persistence.Query;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BankCoOwnerRepository {
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS CO_OWNS_BANK("
                        + "co_owner_uuid UUID NOT NULL,"
                        + "bank_id INTEGER NOT NULL REFERENCES BANK(bank_id),"
                        + "PRIMARY KEY (bank_id, co_owner_uuid))")
                .executeUpdate(con);
    }
    
    public void save(Connection con, UUID coOwner, int bankID) throws SQLException {
        Query.of("INSERT INTO CO_OWNS_BANK VALUES (?, ?)")
                .with(coOwner, bankID)
                .executeUpdate(con);
    }
    
    public void saveAll(Connection con, Collection<OfflinePlayer> coOwners, int bankID) throws SQLException {
        Query.of("INSERT INTO CO_OWNS_BANK VALUES (?, ?)")
                .batch(coOwners)
                .with(coOwner -> Arrays.asList(coOwner.getUniqueId(), bankID))
                .executeUpdate(con);
    }
    
    public void delete(Connection con, int bankID) throws SQLException {
        Query.of("DELETE FROM CO_OWNS_BANK WHERE bank_id = ?")
                .with(bankID)
                .executeUpdate(con);
    }
    
    public void deleteAll(Connection con, Collection<Integer> bankIDs) throws SQLException {
        Query.of("DELETE FROM CO_OWNS_BANK WHERE bank_id IN (%s)")
                .in(bankIDs)
                .executeUpdate(con);
    }
    
    public Set<OfflinePlayer> findByBank(Connection con, int bankID) throws SQLException {
        return Query.of("SELECT co_owner_uuid FROM CO_OWNS_BANK WHERE bank_id = ?")
                .with(bankID)
                .asSet(con, UUID.class, Bukkit::getOfflinePlayer);
    }
    
}
