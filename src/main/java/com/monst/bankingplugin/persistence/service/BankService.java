package com.monst.bankingplugin.persistence.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.BankCoOwnerRepository;
import com.monst.bankingplugin.persistence.repository.BankRepository;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class BankService extends Service {
    
    private static class BankCache {
        
        private final Cache<Integer, Bank> banksById = CacheBuilder.newBuilder().softValues().build();
        private final Cache<String, Bank> banksByName = CacheBuilder.newBuilder().softValues().build();
        
        private void put(Bank bank) {
            banksById.put(bank.getID(), bank);
            banksByName.put(bank.getName(), bank);
        }
        
        private void remove(Bank bank) {
            banksById.invalidate(bank.getID());
            banksByName.invalidate(bank.getName());
        }
        
        private Optional<Bank> getByID(int id) {
            return Optional.ofNullable(banksById.getIfPresent(id));
        }
        
        private Optional<Bank> getByName(String name) {
            return Optional.ofNullable(banksByName.getIfPresent(name));
        }
        
        private Set<Bank> searchByNames(Collection<String> names) {
            Set<Bank> banks = new HashSet<>();
            for (String name : names) {
                getByName(name).ifPresent(banks::add);
            }
            return banks;
        }
        
    }
    
    private final BankCache cache;
    
    private final BankRepository bankRepo;
    private final BankCoOwnerRepository bankCoOwnerRepo;
    
    private AccountService accountService;
    
    public BankService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.cache = new BankCache();
        this.bankRepo = new BankRepository(this::reconstruct);
        this.bankCoOwnerRepo = new BankCoOwnerRepository();
    }
    
    @Override
    public void createTables() {
        execute(bankRepo::createTable);
        execute(bankCoOwnerRepo::createTable);
    }
    
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }
    
    public void save(Bank bank) {
        plugin.debugf("Saving bank %s to the database.", bank);
        transact(con -> {
            bankRepo.save(con, bank);
            if (bank.hasCoOwners())
                bankCoOwnerRepo.saveAll(con, bank.getCoOwners(), bank.getID());
        });
        cache.put(bank);
    }
    
    public void update(Bank bank) {
        plugin.debugf("Updating bank %d in the database.", bank.getID());
        transact(con -> bankRepo.update(con, bank));
    }
    
    public void remove(Bank bank) {
        plugin.debugf("Deleting bank %s from the database.", bank);
        transact(con -> {
            bankCoOwnerRepo.delete(con, bank.getID()); // Delete co-owners first to avoid violating referential integrity
            bankRepo.delete(con, bank.getID());
        });
        cache.remove(bank);
    }
    
    public void removeAll(Collection<Bank> banks) {
        Set<Integer> bankIDs = banks.stream().map(Bank::getID).collect(Collectors.toSet());
        plugin.debugf("Deleting banks with IDs %s from the database.", bankIDs);
        transact(con -> {
            bankCoOwnerRepo.deleteAll(con, bankIDs); // Delete co-owners first to avoid violating referential integrity
            bankRepo.deleteAll(con, bankIDs);
        });
        banks.forEach(cache::remove);
    }
    
    public int count() {
        plugin.debug("Counting all banks in the database.");
        return query(bankRepo::count).orElse(0);
    }
    
    public Set<Bank> findAll() {
        plugin.debug("Fetching all banks from the database.");
        return query(bankRepo::findAll).orElse(Collections.emptySet());
    }
    
    public Promise<List<Bank>> findAll(int offset, int limit) {
        plugin.debugf("Fetching banks %d to %d from the database.", offset, offset + limit);
        return async(con -> bankRepo.findAll(con, offset, limit));
    }
    
    public Bank findByID(int id) {
        Optional<Bank> cached = cache.getByID(id);
        plugin.debugf("Fetching bank with ID %d from the %s.", id, cached.isPresent() ? "cache" : "database");
        return cached.orElseGet(() -> query(con -> bankRepo.findByID(con, id)).orElse(null));
    }
    
    public Bank findByName(String name) {
        Optional<Bank> bank = cache.getByName(name);
        plugin.debugf("Fetching bank with name %s from the %s.", name, bank.isPresent() ? "cache" : "database");
        return bank.orElseGet(() -> query(con -> bankRepo.findByName(con, name)).orElse(null));
    }
    
    public Set<String> findAllNames() {
        plugin.debug("Fetching all bank names from the database.");
        return query(bankRepo::findAllNames).orElse(Collections.emptySet());
    }
    
    public Set<Bank> findByNames(Set<String> names) {
        Set<Bank> cached = cache.searchByNames(names);
        Set<String> cachedNames = cached.stream().map(Bank::getName).collect(Collectors.toSet());
        names.removeAll(cachedNames);
        if (!cachedNames.isEmpty())
            plugin.debugf("Fetching banks with names %s from the cache.", cachedNames);
        if (names.isEmpty())
            return cached;
        plugin.debugf("Fetching banks with names %s from the database.", names);
        Set<Bank> fromDatabase = query(con -> bankRepo.findByNames(con, names)).orElse(Collections.emptySet());
        fromDatabase.forEach(cache::put);
        cached.addAll(fromDatabase);
        return cached;
    }
    
    public int countByOwner(OfflinePlayer owner) {
        plugin.debugf("Counting banks owned by %s in the database.", owner.getName());
        return query(con -> bankRepo.countByOwner(con, owner.getUniqueId())).orElse(0);
    }
    
    public int countByTrustedPlayer(OfflinePlayer trusted) {
        plugin.debugf("Counting banks where %s is trusted in the database.", trusted.getName());
        return query(con -> bankRepo.countByTrustedPlayer(con, trusted.getUniqueId())).orElse(0);
    }
    
    public Set<String> findNamesByOwner(OfflinePlayer owner) {
        plugin.debugf("Fetching names of banks owned by %s from the database.", owner);
        return query(con -> bankRepo.findNamesByOwner(con, owner.getUniqueId())).orElse(Collections.emptySet());
    }
    
    public Set<String> findNamesByTrustedPlayer(OfflinePlayer trusted) {
        plugin.debugf("Fetching bank names where %s is trusted from the database.", trusted);
        return query(con -> bankRepo.findNamesByTrustedPlayer(con, trusted.getUniqueId())).orElse(Collections.emptySet());
    }
    
    public int countPlayerBanks() {
        plugin.debug("Counting all player banks in the database.");
        return query(bankRepo::countPlayerBanks).orElse(0);
    }
    
    public int countAdminBanks() {
        plugin.debug("Counting all admin banks in the database.");
        return query(bankRepo::countAdminBanks).orElse(0);
    }
    
    public Set<String> findPlayerBankNames() {
        plugin.debug("Fetching all player bank names.");
        return query(bankRepo::findPlayerBankNames).orElse(Collections.emptySet());
    }
    
    public Set<String> findAdminBankNames() {
        plugin.debug("Fetching all admin bank names.");
        return query(bankRepo::findAdminBankNames).orElse(Collections.emptySet());
    }
    
    public Bank findContaining(Player player) {
        return findContaining(player.getLocation().getBlock());
    }
    
    public Bank findContaining(Block block) {
        return query(con -> bankRepo.findByWorld(con, block.getWorld().getName())).orElse(Collections.emptySet())
                .stream()
                .filter(bank -> bank.getRegion().contains(block.getX(), block.getY(), block.getZ()))
                .findFirst()
                .orElse(null);
    }
    
    public Bank findContaining(AccountLocation location) {
        return query(con -> bankRepo.findByWorld(con, location.getWorld().getName())).orElse(Collections.emptySet())
                .stream()
                .filter(bank -> bank.getRegion().contains(location))
                .findFirst()
                .orElse(null);
    }
    
    public Set<Bank> findOverlapping(BankRegion region) {
        return query(con -> bankRepo.findByWorld(con, region.getWorld().getName())).orElse(Collections.emptySet())
                .stream()
                .filter(bank -> bank.getRegion().overlaps(region))
                .collect(Collectors.toSet());
    }
    
    public Set<String> findNamesByPlayerAllowedToModify(Player player, boolean hasPlayerPerm, boolean hasAdminPerm, boolean mustBeOwner) {
        if (hasPlayerPerm) {
            if (hasAdminPerm)
                return findAllNames();
            return findPlayerBankNames();
        }
        Set<String> ownBanks = mustBeOwner ? findNamesByOwner(player) : findNamesByTrustedPlayer(player);
        if (hasAdminPerm) {
            Set<String> bankNames = findAdminBankNames();
            bankNames.addAll(ownBanks);
            return bankNames;
        } else
            return ownBanks;
    }
    
    private Bank reconstruct(ResultSet rs, Connection con) throws SQLException {
        int bankID = rs.getInt("bank_id");
        Optional<Bank> cached = cache.getByID(bankID);
        if (cached.isPresent())
            return cached.get();
        UUID ownerUUID = rs.getObject("owner_uuid", UUID.class);
        Bank bank = new Bank(
                bankID,
                rs.getString("name"),
                ownerUUID == null ? null : Bukkit.getOfflinePlayer(ownerUUID),
                reconstructRegion(rs),
                bankCoOwnerRepo.findByBank(con, bankID),
                rs.getBigDecimal("interest_rate"),
                rs.getBigDecimal("account_creation_price"),
                rs.getBigDecimal("minimum_balance"),
                rs.getBigDecimal("low_balance_fee"),
                rs.getObject("allowed_offline_payouts", Integer.class),
                rs.getObject("offline_multiplier_decrement", Integer.class),
                rs.getObject("withdrawal_multiplier_decrement", Integer.class),
                rs.getObject("player_bank_account_limit", Integer.class),
                rs.getObject("reimburse_account_creation", Boolean.class),
                rs.getObject("pay_on_low_balance", Boolean.class),
                toIntList(rs.getArray("interest_multipliers")),
                toTimeList(rs.getArray("interest_payout_times"))
        );
        cache.put(bank);
        return bank;
    }
    
    private BankRegion reconstructRegion(ResultSet rs) throws SQLException {
        return BankRegion.fromDatabase(
                Bukkit.getWorld(rs.getString("world")),
                rs.getObject("min_x", Integer.class),
                rs.getInt("min_y"),
                rs.getObject("min_z", Integer.class),
                rs.getObject("max_x", Integer.class),
                rs.getInt("max_y"),
                rs.getObject("max_z", Integer.class),
                toIntArray(rs.getArray("points_x")),
                toIntArray(rs.getArray("points_z"))
        );
    }
    
    private static int[] toIntArray(Array array) throws SQLException {
        return array == null ? null : (int[]) array.getArray();
    }
    
    private static List<Integer> toIntList(Array array) throws SQLException {
        if (array == null)
            return null;
        return Arrays.asList((Integer[]) array.getArray());
    }
    
    private static Set<LocalTime> toTimeList(Array array) throws SQLException {
        if (array == null)
            return null;
        return Arrays.stream((Object[]) array.getArray()).map(o -> ((Time) o).toLocalTime()).collect(Collectors.toSet());
    }
    
}
