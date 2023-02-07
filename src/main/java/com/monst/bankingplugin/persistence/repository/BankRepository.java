package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.entity.geo.region.CuboidBankRegion;
import com.monst.bankingplugin.entity.geo.region.PolygonalBankRegion;
import com.monst.bankingplugin.persistence.Query;
import com.monst.bankingplugin.persistence.Reconstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BankRepository {
    
    private final Reconstructor<Bank> reconstructor;
    
    public BankRepository(Reconstructor<Bank> reconstructor) {
        this.reconstructor = reconstructor;
    }
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS BANK("
                        + "bank_id INTEGER NOT NULL PRIMARY KEY,"
                        + "name VARCHAR(255) NOT NULL UNIQUE,"
                        + "owner_uuid UUID,"
                        + "interest_rate NUMERIC(8,4),"
                        + "account_creation_price NUMERIC(16,2),"
                        + "minimum_balance NUMERIC(16,2),"
                        + "low_balance_fee NUMERIC(16,2),"
                        + "allowed_offline_payouts INTEGER,"
                        + "offline_multiplier_decrement INTEGER,"
                        + "withdrawal_multiplier_decrement INTEGER,"
                        + "player_bank_account_limit INTEGER,"
                        + "reimburse_account_creation BOOLEAN,"
                        + "pay_on_low_balance BOOLEAN,"
                        + "interest_multipliers INTEGER ARRAY,"
                        + "interest_payout_times TIME ARRAY,"
                        + "world VARCHAR(32) NOT NULL,"
                        + "min_x INTEGER,"
                        + "min_y INTEGER NOT NULL,"
                        + "min_z INTEGER,"
                        + "max_x INTEGER,"
                        + "max_y INTEGER NOT NULL,"
                        + "max_z INTEGER,"
                        + "points_x INTEGER ARRAY,"
                        + "points_z INTEGER ARRAY)"
                ).executeUpdate(con);
        Query.of("ALTER TABLE BANK ADD CONSTRAINT IF NOT EXISTS VERTEX_CHECK "
                        + "CHECK (CARDINALITY(points_x) = CARDINALITY(points_z))")
                .executeUpdate(con);
    }

    public void save(Connection con, Bank bank) throws SQLException {
        Query.of("INSERT INTO BANK VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .with(bank.getID())
                .and(deconstruct(bank))
                .executeUpdate(con);
    }
    
    public void update(Connection con, Bank bank) throws SQLException {
        Query.of("UPDATE BANK SET name = ?, owner_uuid = ?, interest_rate = ?, account_creation_price = ?, "
                        + "minimum_balance = ?, low_balance_fee = ?, allowed_offline_payouts = ?, "
                        + "offline_multiplier_decrement = ?, withdrawal_multiplier_decrement = ?, "
                        + "player_bank_account_limit = ?, reimburse_account_creation = ?, pay_on_low_balance = ?, "
                        + "interest_multipliers = ?, interest_payout_times = ?, world = ?, min_x = ?, min_y = ?, min_z = ?, "
                        + "max_x = ?, max_y = ?, max_z = ?, points_x = ?, points_z = ? WHERE bank_id = ?")
                .with(deconstruct(bank))
                .and(bank.getID())
                .executeUpdate(con);
    }
    
    public void delete(Connection con, int bankID) throws SQLException {
        Query.of("DELETE FROM BANK WHERE bank_id = ?")
                .with(bankID)
                .executeUpdate(con);
    }
    
    public void deleteAll(Connection con, Collection<Integer> bankIDs) throws SQLException {
        Query.of("DELETE FROM BANK WHERE bank_id IN (%s)")
                .in(bankIDs)
                .executeUpdate(con);
    }
    
    public int count(Connection con) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM BANK")
                .asOne(con, Integer.class);
    }
    
    public Set<Bank> findAll(Connection con) throws SQLException {
        return Query.of("SELECT * FROM BANK")
                .asSet(con, reconstructor);
    }
    
    public List<Bank> findAll(Connection con, int offset, int limit) throws SQLException {
        return Query.of("SELECT * FROM BANK OFFSET ? ROWS LIMIT ?")
                .with(offset, limit)
                .asList(con, reconstructor);
    }
    
    public Bank findByID(Connection con, int bankID) throws SQLException {
        return Query.of("SELECT * FROM BANK WHERE bank_id = ?")
                .with(bankID)
                .asOne(con, reconstructor);
    }
    
    // TODO: Test color codes and case sensitivity
    public Bank findByName(Connection con, String name) throws SQLException {
        return Query.of("SELECT * FROM BANK WHERE name = ?")
                .with(name)
                .asOne(con, reconstructor);
    }
    
    public Set<String> findAllNames(Connection con) throws SQLException {
        return Query.of("SELECT name FROM BANK")
                .asSet(con, String.class);
    }
    
    public Set<Bank> findByNames(Connection con, Collection<String> names) throws SQLException {
        return Query.of("SELECT * FROM BANK WHERE name IN (%s)")
                .in(names)
                .asSet(con, reconstructor);
    }
    
    public Set<Bank> findByWorld(Connection con, String worldName) throws SQLException {
        return Query.of("SELECT * FROM BANK WHERE world = ?")
                .with(worldName)
                .asSet(con, reconstructor);
    }
    
    public int countByOwner(Connection con, UUID owner) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM BANK WHERE owner_uuid = ?")
                .with(owner)
                .asOne(con, Integer.class);
    }
    
    public int countByTrustedPlayer(Connection con, UUID trusted) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM BANK WHERE owner_uuid = ? OR bank_id IN "
                        + "(SELECT bank_id FROM CO_OWNS_BANK WHERE co_owner_uuid = ?)")
                .with(trusted, trusted)
                .asOne(con, Integer.class);
    }
    
    public Set<String> findNamesByOwner(Connection con, UUID owner) throws SQLException {
        return Query.of("SELECT name FROM BANK WHERE owner_uuid = ?")
                .with(owner)
                .asSet(con, String.class);
    }
    
    public Set<String> findNamesByTrustedPlayer(Connection con, UUID trusted) throws SQLException {
        return Query.of("SELECT name FROM BANK WHERE owner_uuid = ? OR bank_id IN "
                        + "(SELECT bank_id FROM CO_OWNS_BANK WHERE co_owner_uuid = ?)")
                .with(trusted, trusted)
                .asSet(con, String.class);
    }
    
    public int countPlayerBanks(Connection con) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM BANK WHERE owner_uuid IS NOT NULL")
                .asOne(con, Integer.class);
    }
    
    public Set<String> findPlayerBankNames(Connection con) throws SQLException {
        return Query.of("SELECT name FROM BANK WHERE owner_uuid IS NOT NULL")
                .asSet(con, String.class);
    }
    
    public int countAdminBanks(Connection con) throws SQLException {
        return Query.of("SELECT COUNT(*) FROM BANK WHERE owner_uuid IS NULL")
                .asOne(con, Integer.class);
    }
    
    public Set<String> findAdminBankNames(Connection con) throws SQLException {
        return Query.of("SELECT name FROM BANK WHERE owner_uuid IS NULL")
                .asSet(con, String.class);
    }
    
    private List<Object> deconstruct(Bank bank) {
        BankRegion region = bank.getRegion();
        return Arrays.asList(
                bank.getName(),
                bank.getOwner() == null ? null : bank.getOwner().getUniqueId(),
                bank.getInterestRate(),
                bank.getAccountCreationPrice(),
                bank.getMinimumBalance(),
                bank.getLowBalanceFee(),
                bank.getAllowedOfflinePayouts(),
                bank.getOfflineMultiplierDecrement(),
                bank.getWithdrawalMultiplierDecrement(),
                bank.getPlayerBankAccountLimit(),
                bank.reimbursesAccountCreation(),
                bank.paysOnLowBalance(),
                bank.getInterestMultipliers() == null ? null : bank.getInterestMultipliers().toArray(),
                bank.getInterestPayoutTimes() == null ? null : bank.getInterestPayoutTimes().toArray(),
                region.getWorld().getName(),
                region instanceof CuboidBankRegion ? region.getMinX() : null,
                region.getMinY(),
                region instanceof CuboidBankRegion ? region.getMinZ() : null,
                region instanceof CuboidBankRegion ? region.getMaxX() : null,
                region.getMaxY(),
                region instanceof CuboidBankRegion ? region.getMaxZ() : null,
                region instanceof PolygonalBankRegion ? ((PolygonalBankRegion) region).getPointsX() : null,
                region instanceof PolygonalBankRegion ? ((PolygonalBankRegion) region).getPointsZ() : null
        );
    }

}
