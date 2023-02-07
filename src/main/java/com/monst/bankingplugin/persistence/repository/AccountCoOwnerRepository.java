package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.persistence.Query;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class AccountCoOwnerRepository {
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS CO_OWNS_ACCOUNT("
                        + "co_owner_uuid UUID NOT NULL,"
                        + "account_id INTEGER NOT NULL REFERENCES ACCOUNT(account_id),"
                        + "PRIMARY KEY (account_id, co_owner_uuid))")
                .executeUpdate(con);
    }
    
    public void save(Connection con, UUID coOwnerUUID, int accountID) throws SQLException {
        Query.of("INSERT INTO CO_OWNS_ACCOUNT VALUES (?, ?)")
                .with(coOwnerUUID, accountID)
                .executeUpdate(con);
    }
    
    public void saveAll(Connection con, Collection<OfflinePlayer> coOwners, int accountID) throws SQLException {
        Query.of("INSERT INTO CO_OWNS_ACCOUNT VALUES (?, ?)")
                .batch(coOwners)
                .with(coOwner -> Arrays.asList(coOwner.getUniqueId(), accountID))
                .executeUpdate(con);
    }
    
    public void delete(Connection con, int accountID) throws SQLException {
        Query.of("DELETE FROM CO_OWNS_ACCOUNT WHERE account_id = ?")
                .with(accountID)
                .executeUpdate(con);
    }
    
    public void deleteAll(Connection con, Collection<Integer> accountIDs) throws SQLException {
        Query.of("DELETE FROM CO_OWNS_ACCOUNT WHERE account_id IN (%s)")
                .in(accountIDs)
                .executeUpdate(con);
    }
    
    public Set<OfflinePlayer> findByAccount(Connection con, int accountID) throws SQLException {
        return Query.of("SELECT co_owner_uuid FROM CO_OWNS_ACCOUNT WHERE account_id = ?")
                .with(accountID)
                .asSet(con, UUID.class, Bukkit::getOfflinePlayer);
    }
    
}
