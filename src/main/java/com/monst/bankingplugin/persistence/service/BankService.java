package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.Vector3;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.persistence.repository.BankRepository;
import com.monst.bankingplugin.util.Permission;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BankService extends EntityService<Bank, BankRepository> {

    public BankService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new BankRepository(emf));
    }

    public int count() {
        return repo.count();
    }

    public Bank findContaining(Player player) {
        return findContaining(player.getLocation().getBlock());
    }

    public Bank findContaining(Block block) {
        return repo.findByWorld(block.getWorld()).stream()
                .filter(bank -> bank.getRegion().contains(block.getX(), block.getY(), block.getZ()))
                .findFirst()
                .orElse(null);
    }

    public Bank findContaining(AccountLocation location) {
        Vector3 min = location.getMinimumBlock();
        return repo.findByWorld(location.getWorld()).stream()
                .filter(bank -> bank.getRegion().contains(min))
                .findFirst()
                .orElse(null);
    }

    public List<Bank> findOverlapping(BankRegion region) {
        return repo.findByWorld(region.getWorld()).stream()
                .filter(bank -> bank.getRegion().overlaps(region))
                .collect(Collectors.toList());
    }

    public Bank findByName(String name) {
        return repo.findByName(name);
    }

    public List<Bank> findByNameIn(Collection<String> names) {
        return repo.findByNameIn(names);
    }

    public List<Bank> findByNameStartsWith(String name) {
        return repo.findByNameStartsWith(name);
    }

    public List<Bank> findByOwner(OfflinePlayer owner) {
        return repo.findByOwner(owner);
    }

    public List<Bank> findByTrustedPlayer(OfflinePlayer trusted) {
        return repo.findByTrustedPlayer(trusted);
    }

    public List<Bank> findAdminBanks() {
        return repo.findAdminBanks();
    }

    public List<Bank> findPlayerBanks() {
        return repo.findPlayerBanks();
    }

    public List<Bank> findByPlayerAllowedToModify(Player player, Permission playerPerm, Permission adminPerm, boolean mustBeOwner) {
        final boolean hasPlayerPerm = playerPerm.ownedBy(player);
        final boolean hasAdminPerm = adminPerm.ownedBy(player);
        if (hasPlayerPerm) {
            if (hasAdminPerm)
                return findAll();
            return findPlayerBanks();
        }
        List<Bank> ownBanks = mustBeOwner ? findByOwner(player) : findByTrustedPlayer(player);
        if (hasAdminPerm) {
            List<Bank> banks = findAdminBanks();
            banks.addAll(ownBanks);
            return banks;
        } else
            return ownBanks;
    }

    public void remove(Bank bank) {
        transaction(em -> em.remove(em.getReference(Bank.class, bank.getID())));
    }

    public void removeAll(Collection<Bank> banks) {
        transaction(em -> banks.stream().map(bank -> em.getReference(Bank.class, bank.getID())).forEach(em::remove));
    }

}
