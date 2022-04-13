package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.QAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountRepository extends EntityRepository<Account> {

    static final QAccount account = QAccount.account;

    public AccountRepository(Supplier<EntityManager> emf) {
        super(emf, account);
    }

    public int count() {
        EntityManager reader = emf.get();
        int count = (int) (long) reader.createNativeQuery("SELECT COUNT(*) FROM ACCOUNT").getSingleResult();
        reader.close();
        return count;
    }

    public Account findByID(int id) {
        return entities().where(account.id.eq(id)).fetchOne();
    }

    public int countByOwner(OfflinePlayer owner) {
        EntityManager reader = emf.get();
        Query query = reader.createNativeQuery("SELECT COUNT(*) FROM ACCOUNT WHERE ACCOUNT.OWNER = ?");
        query.setParameter(1, owner.getUniqueId());
        int count = (int) (long) query.getSingleResult();
        reader.close();
        return count;
    }

    public List<Account> findByOwnerIn(Collection<OfflinePlayer> owners) {
        return entities().where(account.owner.in(owners)).fetch();
    }

    public List<Account> findByBanks(Collection<Bank> banks) {
        return entities().where(account.bank.in(banks)).fetch();
    }

    public List<Account> findByBankAndOwner(Bank bank, OfflinePlayer owner) {
        return entities().where(account.bank.eq(bank), account.owner.eq(owner)).fetch();
    }

    public List<Account> findByTrustedPlayer(OfflinePlayer trusted) {
        return entities().where(account.owner.eq(trusted).or(account.co_owners.contains(trusted))).fetch();
    }

    public Account findAt(Block block) {
        EntityManager reader = emf.get();
        Query query = reader.createNativeQuery(
                "SELECT a.* FROM ACCOUNT a INNER JOIN ACCOUNT_LOCATION loc on loc.ID = a.LOCATION_ID " +
                        "WHERE loc.WORLD = ? AND ? IN (loc.X1, loc.X2) AND loc.Y = ? AND ? IN (loc.Z1, loc.Z2)",
                Account.class);
        query.setParameter(1, block.getWorld().getName());
        query.setParameter(2, block.getX());
        query.setParameter(3, block.getY());
        query.setParameter(4, block.getZ());
        @SuppressWarnings("unchecked")
        Stream<Account> result = (Stream<Account>) query.getResultStream();
        reader.close();
        return result.findAny().orElse(null);
    }

    public List<Account> findAt(Collection<Block> blocks) {
        StringBuilder queryBuilder = new StringBuilder(256);
        queryBuilder.append("SELECT a.* FROM ACCOUNT a INNER JOIN ACCOUNT_LOCATION loc on loc.ID = a.LOCATION_ID WHERE ");
        for (int i = 0; i < blocks.size(); i++) {
            queryBuilder.append("(loc.WORLD = ? AND ? IN (loc.X1, loc.X2) AND loc.Y = ? AND ? IN (loc.Z1, loc.Z2)) OR ");
        }
        queryBuilder.setLength(queryBuilder.length() - 4); // Remove last OR

        EntityManager reader = emf.get();
        Query query = reader.createNativeQuery(queryBuilder.toString(), Account.class);
        int i = 1;
        for (Block block : blocks) {
            query.setParameter(i++, block.getWorld().getName());
            query.setParameter(i++, block.getX());
            query.setParameter(i++, block.getY());
            query.setParameter(i++, block.getZ());
        }
        @SuppressWarnings("unchecked")
        Stream<Account> result = (Stream<Account>) query.getResultStream();
        reader.close();
        return result.collect(Collectors.toList());
    }

}
