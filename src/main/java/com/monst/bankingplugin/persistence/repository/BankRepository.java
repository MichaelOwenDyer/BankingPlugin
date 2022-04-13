package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.QBank;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class BankRepository extends EntityRepository<Bank> {

    static final QBank bank = QBank.bank;

    public BankRepository(Supplier<EntityManager> emf) {
        super(emf, bank);
    }

    public int count() {
        EntityManager reader = emf.get();
        int count = (int) (long) reader.createNativeQuery("SELECT COUNT(*) FROM BANK").getSingleResult();
        reader.close();
        return count;
    }

    public Bank findById(Integer id) {
        return entities().where(bank.id.eq(id)).fetchOne();
    }

    public Bank findByName(String name) {
        return entities().where(bank.name.equalsIgnoreCase(name)).fetchOne();
    }

    public List<Bank> findByNameIn(Collection<String> names) {
        return entities().where(bank.name.in(names)).fetch();
    }

    public List<Bank> findByNameStartsWith(String name) {
        return entities().where(bank.name.startsWithIgnoreCase(name)).fetch();
    }

    public List<Bank> findByWorld(World world) {
        return entities().where(bank.region.world.eq(world)).fetch();
    }

    public List<Bank> findByOwner(OfflinePlayer owner) {
        return entities().where(bank.owner.eq(owner)).fetch();
    }

    public List<Bank> findByTrustedPlayer(OfflinePlayer trusted) {
        return entities().where(bank.owner.eq(trusted).or(bank.co_owners.contains(trusted))).fetch();
    }

    public List<Bank> findAdminBanks() {
        return entities().where(bank.owner.isNull()).fetch();
    }

    public List<Bank> findPlayerBanks() {
        return entities().where(bank.owner.isNotNull()).fetch();
    }

}
