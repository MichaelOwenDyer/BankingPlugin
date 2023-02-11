package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.persistence.Query;
import com.monst.bankingplugin.persistence.Reconstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountRepository {
    
    private final Reconstructor<Account> reconstructor;
    
    public AccountRepository(Reconstructor<Account> reconstructor) {
        this.reconstructor = reconstructor;
    }
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS ACCOUNT("
                        + "account_id INTEGER NOT NULL PRIMARY KEY,"
                        + "bank_id INTEGER NOT NULL REFERENCES BANK(bank_id),"
                        + "owner_uuid UUID NOT NULL,"
                        + "balance NUMERIC(16,2) NOT NULL,"
                        + "previous_balance NUMERIC(16,2) NOT NULL,"
                        + "multiplier_stage INTEGER,"
                        + "remaining_offline_payouts INTEGER,"
                        + "custom_name VARCHAR(255),"
                        + "world VARCHAR(32) NOT NULL,"
                        + "X1 INTEGER NOT NULL,"
                        + "Y INTEGER NOT NULL,"
                        + "Z1 INTEGER NOT NULL,"
                        + "X2 INTEGER," // X2 and Z2 are null if the account is a single chest
                        + "Z2 INTEGER)")
                .executeUpdate(con);
        Query.of("ALTER TABLE ACCOUNT ADD CONSTRAINT IF NOT EXISTS BOTH_XZ_NULL_OR_NEITHER CHECK "
                        + "((X1 IS NULL AND Z1 IS NULL) OR (X1 IS NOT NULL AND Z1 IS NOT NULL))")
                .executeUpdate(con);
        Query.of("ALTER TABLE ACCOUNT ADD CONSTRAINT IF NOT EXISTS XZ_ADJACENT CHECK "
                        + "(X2 IS NULL OR ((X1 = X2 AND Z1 + 1 = Z2) OR (Z1 = Z2 AND X1 + 1 = X2)))")
                .executeUpdate(con);
    }
    
    // WRITE
    
    public void save(Connection con, Account account) throws SQLException {
        Query.of("INSERT INTO ACCOUNT VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .with(account.getID())
                .and(deconstruct(account))
                .executeUpdate(con);
    }
    
    public void update(Connection con, Account account) throws SQLException {
        Query.of("UPDATE ACCOUNT SET bank_id = ?, owner_uuid = ?, balance = ?, previous_balance = ?, "
                        + "multiplier_stage = ?, remaining_offline_payouts = ?, custom_name = ?, "
                        + "world = ?, X1 = ?, Y = ?, Z1 = ?, X2 = ?, Z2 = ? WHERE account_id = ?")
                .with(deconstruct(account))
                .and(account.getID())
                .executeUpdate(con);
    }
    
    public void updateAll(Connection con, Collection<Account> accounts) throws SQLException {
        Query.of("UPDATE ACCOUNT SET bank_id = ?, owner_uuid = ?, balance = ?, previous_balance = ?, "
                        + "multiplier_stage = ?, remaining_offline_payouts = ?, custom_name = ?, "
                        + "world = ?, X1 = ?, Y = ?, Z1 = ?, X2 = ?, Z2 = ? WHERE account_id = ?")
                .batch(accounts)
                .with(this::deconstruct)
                .and(Account::getID)
                .executeUpdate(con);
    }
    
    public void delete(Connection con, int accountID) throws SQLException {
        Query.of("DELETE FROM ACCOUNT WHERE account_id = ?")
                .with(accountID)
                .executeUpdate(con);
    }
    
    public void deleteAll(Connection con, Collection<Integer> accountIDs) throws SQLException {
        Query.of("DELETE FROM ACCOUNT WHERE account_id IN (%s)")
                .in(accountIDs)
                .executeUpdate(con);
    }
    
    // READ
    
    public Integer count(Connection con) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM ACCOUNT")
                .asOne(con, Integer.class);
    }
    
    public Set<Account> findAll(Connection con) throws SQLException {
        return Query.of("SELECT * FROM Account")
                .asSet(con, reconstructor);
    }
    
    public List<Account> findAll(Connection con, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM Account ORDER BY account_id OFFSET ? ROWS LIMIT ?")
                .with(offset, limit)
                .asList(con, reconstructor);
    }
    
    public Account findByID(Connection con, int accountID) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE account_id = ?")
                .with(accountID)
                .asOne(con, reconstructor);
    }
    
    public int countByOwner(Connection con, UUID ownerUUID) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE owner_uuid = ?")
                .with(ownerUUID)
                .asOne(con, Integer.class);
    }
    
    public int countByTrustedPlayer(Connection con, UUID trusted) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE owner_uuid = ? OR account_id IN "
                        + "(SELECT account_id FROM CO_OWNS_ACCOUNT WHERE co_owner_uuid = ?)")
                .with(trusted, trusted)
                .asOne(con, Integer.class);
    }
    
    public List<Account> findByTrustedPlayer(Connection con, UUID trusted, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE owner_uuid = ? OR account_id IN "
                        + "(SELECT account_id FROM CO_OWNS_ACCOUNT WHERE co_owner_uuid = ?)"
                        + "ORDER BY account_id OFFSET ? ROWS LIMIT ?")
                .with(trusted, trusted)
                .and(offset, limit)
                .asList(con, reconstructor);
    }
    
    public int countByOwners(Connection con, Collection<OfflinePlayer> owners) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE owner_uuid IN (%s)")
                .in(owners, OfflinePlayer::getUniqueId)
                .asOne(con, Integer.class);
    }
    
    public List<Account> findByOwners(Connection con, Set<OfflinePlayer> owners, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE owner_uuid IN (%s) ORDER BY account_id OFFSET ? ROWS LIMIT ?")
                .in(owners, OfflinePlayer::getUniqueId)
                .with(offset, limit)
                .asList(con, reconstructor);
    }
    
    public Set<Account> findByOwners(Connection con, Collection<OfflinePlayer> owners) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE owner_uuid IN (%s)")
                .in(owners, OfflinePlayer::getUniqueId)
                .asSet(con, reconstructor);
    }
    
    public int countByBank(Connection con, int bankID) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE bank_id = ?")
                .with(bankID)
                .asOne(con, Integer.class);
    }
    
    public List<Account> findByBank(Connection con, int bankID, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE bank_id = ? ORDER BY account_id OFFSET ? ROWS LIMIT ?")
                .with(bankID)
                .and(offset, limit)
                .asList(con, reconstructor);
    }
    
    public Set<Account> findByBanks(Connection con, Collection<Integer> bankIDs) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE bank_id IN (%s)")
                .in(bankIDs)
                .asSet(con, reconstructor);
    }
    
    public int countByBankAndOwner(Connection con, int bankID, UUID ownerUUID) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE bank_id = ? AND owner_uuid = ?")
                .with(bankID, ownerUUID)
                .asOne(con, Integer.class);
    }
    
    public Account findAtChest(Connection con, Block chest) throws SQLException {
        return Query.of("SELECT * FROM Account WHERE world = ? AND ? IN (x1, x2) AND ? = y AND ? IN (z1, z2)")
                .with(chest.getWorld().getName(), chest.getX(), chest.getY(), chest.getZ())
                .asOne(con, reconstructor);
    }
    
    public boolean isAccount(Connection con, Block chest) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM Account WHERE world = ? AND ? IN (x1, x2) AND ? = y AND ? IN (z1, z2)")
                .with(chest.getWorld().getName(), chest.getX(), chest.getY(), chest.getZ())
                .asOne(con, Integer.class) > 0;
    }
    
    public Set<Account> findAtChests(Connection con, Collection<Block> chests) throws SQLException {
        String sql = "SELECT * FROM Account WHERE " + chests.stream()
                .map(block -> "(world = ? AND ? IN (x1, x2) AND ? = y AND ? IN (z1, z2))")
                .collect(Collectors.joining(" OR "));
        List<Object> params = chests.stream()
                .flatMap(block -> Stream.of(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()))
                .collect(Collectors.toList());
        return Query.of(sql)
                .with(params)
                .asSet(con, reconstructor);
    }
    
    public int countAccountsAtChests(Connection con, Collection<Block> chests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Account WHERE " + chests.stream()
                .map(block -> "(world = ? AND ? IN (x1, x2) AND ? = y AND ? IN (z1, z2))")
                .collect(Collectors.joining(" OR "));
        List<Object> params = chests.stream()
                .flatMap(block -> Stream.of(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()))
                .collect(Collectors.toList());
        return Query.of(sql)
                .with(params)
                .asOne(con, Integer.class);
    }
    
    private List<Object> deconstruct(Account account) {
        AccountLocation location = account.getLocation();
        return Arrays.asList(
                account.getBank().getID(),
                account.getOwner().getUniqueId(),
                account.getBalance(),
                account.getPreviousBalance(),
                account.getInterestMultiplierStage(),
                account.getRemainingOfflinePayouts(),
                account.getCustomName(),
                location.getWorld().getName(),
                location.getMinimumBlock().getX(),
                location.getY(),
                location.getMinimumBlock().getZ(),
                location.getSize() == 1 ? null : location.getMaximumBlock().getX(),
                location.getSize() == 1 ? null : location.getMaximumBlock().getZ()
        );
    }
    
}
