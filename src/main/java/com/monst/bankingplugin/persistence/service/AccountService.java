package com.monst.bankingplugin.persistence.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.persistence.ConnectionSupplier;
import com.monst.bankingplugin.persistence.repository.AccountCoOwnerRepository;
import com.monst.bankingplugin.persistence.repository.AccountRepository;
import com.monst.bankingplugin.util.Observable;
import com.monst.bankingplugin.util.Observer;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AccountService extends Service implements Observable {
    
    private BankService bankService;

    private final AccountRepository accountRepo;
    private final AccountCoOwnerRepository coOwnerRepo;
    
    private final Cache<Integer, Account> accountsById = CacheBuilder.newBuilder().softValues().build();
    private final Cache<Block, Account> accountsByBlock = CacheBuilder.newBuilder().softValues().build();
    
    private final Set<Observer> observers;

    public AccountService(BankingPlugin plugin, ConnectionSupplier connectionSupplier) {
        super(plugin, connectionSupplier);
        this.accountRepo = new AccountRepository(this::reconstruct); // Caching is done in this method
        this.coOwnerRepo = new AccountCoOwnerRepository();
        this.observers = new HashSet<>();
    }
    
    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }
    
    @Override
    public Set<Observer> getObservers() {
        return observers;
    }
    
    @Override
    public void createTables() {
        execute(accountRepo::createTable);
        execute(coOwnerRepo::createTable);
    }
    
    public void save(Account account) {
        plugin.debug("Saving account %s to the database.", account);
        transact(con -> {
            accountRepo.save(con, account);
            if (account.hasCoOwners())
                coOwnerRepo.saveAll(con, account.getCoOwners(), account.getID());
        });
        cache(account);
    }

    public void update(Account account) {
        plugin.debug("Updating account in the database: " + account);
        transact(con -> accountRepo.update(con, account));
    }

    public void updateAll(Collection<Account> accounts) {
        for (Account account : accounts)
            plugin.debug("Updating account in the database: " + account);
        transact(con -> accountRepo.updateAll(con, accounts));
    }

    public void remove(Account account) {
        int accountID = account.getID();
        plugin.debug("Deleting account from the database: " + account);
        transact(con -> {
            coOwnerRepo.delete(con, accountID); // Delete co-owners first to avoid violating referential integrity
            accountRepo.delete(con, accountID);
        });
        uncache(account);
    }
    
    public void removeAll(Collection<Account> accounts) {
        Set<Integer> accountIDs = accounts.stream().map(Account::getID).collect(Collectors.toSet());
        plugin.debug("Deleting accounts with IDs %s from the database.", accountIDs);
        transact(con -> {
            coOwnerRepo.deleteAll(con, accountIDs); // Delete co-owners first to avoid violating referential integrity
            accountRepo.deleteAll(con, accountIDs);
        });
        accounts.forEach(this::uncache);
    }
    
    public int count() {
        return query(accountRepo::count).orElse(0);
    }

    public Set<Account> findAll() {
        plugin.debug("Fetching all accounts from the database.");
        return query(accountRepo::findAll).orElse(Collections.emptySet());
    }
    
    public Promise<List<Account>> findAll(int offset, int limit) {
        plugin.debug("Fetching all accounts from the database asynchronously.");
        return async(con -> accountRepo.findAll(con, offset, limit));
    }

    public int countByOwner(OfflinePlayer owner) {
        plugin.debug("Counting accounts owned by %s in the database.", owner.getName());
        return query(con -> accountRepo.countByOwner(con, owner.getUniqueId())).orElse(0);
    }
    
    public Promise<Integer> countByTrustedPlayer(OfflinePlayer trusted) {
        plugin.debug("Counting accounts where %s is trusted in the database asynchronously.", trusted.getName());
        return async(con -> accountRepo.countByTrustedPlayer(con, trusted.getUniqueId()));
    }
    
    public Promise<List<Account>> findByTrustedPlayer(OfflinePlayer trusted, int offset, int limit) {
        plugin.debug("Fetching accounts where %s is trusted from the database.", trusted.getName());
        return async(con -> accountRepo.findByTrustedPlayer(con, trusted.getUniqueId(), offset, limit));
    }
    
    public Promise<Integer> countByOwners(Collection<OfflinePlayer> owners) {
        plugin.debug("Counting accounts owned by %s in the database.", owners);
        return async(con -> accountRepo.countByOwners(con, owners));
    }
    
    public Promise<List<Account>> findByOwners(Set<OfflinePlayer> owners, int offset, int limit) {
        plugin.debug("Fetching accounts owned by %s from the database asynchronously.", owners);
        return async(con -> accountRepo.findByOwners(con, owners, offset, limit));
    }

    public Set<Account> findByOwners(Collection<OfflinePlayer> owners) {
        plugin.debug("Fetching accounts owned by %s from the database.", owners);
        return query(con -> accountRepo.findByOwners(con, owners)).orElse(Collections.emptySet());
    }
    
    public Promise<Integer> countByBank(Bank bank) {
        plugin.debug("Counting accounts at bank %s in the database asynchronously.", bank);
        return async(con -> accountRepo.countByBank(con, bank.getID()));
    }
    
    public Promise<List<Account>> findByBank(Bank bank, int offset, int limit) {
        plugin.debug("Fetching accounts at bank %s from the database asynchronously.", bank);
        return async(con -> accountRepo.findByBank(con, bank.getID(), offset, limit));
    }

    public Set<Account> findByBanks(Collection<Bank> banks) {
        plugin.debug("Fetching accounts at banks %s from the database.", banks);
        return query(con -> accountRepo.findByBanks(con, banks.stream().map(Bank::getID).collect(Collectors.toList())))
                .orElse(Collections.emptySet());
    }
    
    public int countByBankAndOwner(Bank bank, OfflinePlayer owner) {
        plugin.debug("Counting accounts at bank %s owned by %s in the database.", bank, owner.getName());
        return query(con -> accountRepo.countByBankAndOwner(con, bank.getID(), owner.getUniqueId())).orElse(0);
    }

    public Promise<List<Account>> findAllMissing(int offset, int limit) {
        plugin.debug("Fetching all missing accounts from the database asynchronously.");
        return async(con -> accountRepo.findAll(con).stream()
                .filter(account -> !account.getLocation().findChest().isPresent())
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList()));
    }

    public Account findAtChest(Block chest) {
        plugin.debug("Fetching account at chest %s from the database.", chest);
        return query(con -> accountRepo.findAtChest(con, chest)).orElse(null);
    }
    
    /**
     * Returns whether the given block is an account.
     * @param block the block to check
     * @return whether the given block is an account
     */
    public boolean isAccount(Block block) {
        return query(con -> accountRepo.isAccount(con, block)).orElse(false);
    }

    public Set<Account> findAtBlocks(Collection<Block> chests) {
        plugin.debug("Fetching accounts at chests %s from the database.", chests);
        return query(con -> accountRepo.findAtChests(con, chests)).orElse(Collections.emptySet());
    }

    /**
     * Returns whether any of the given blocks are accounts.
     * @param blocks the blocks to check
     * @return whether any of the given blocks are accounts
     */
    public boolean isAnyAccount(Collection<Block> blocks) {
        // If cache contains any of the blocks, then it's an account
        return query(con -> accountRepo.countAccountsAtChests(con, blocks)).orElse(0) > 0;
    }

    private Account reconstruct(ResultSet rs, Connection con) throws SQLException {
        int accountID = rs.getInt("account_id");
        Account cached = accountsById.getIfPresent(accountID);
        if (cached != null)
            return cached;
        Bank bank = bankService.findByID(rs.getInt("bank_id"));
        Account account = new Account(
                accountID,
                bank,
                Bukkit.getOfflinePlayer(rs.getObject("owner_uuid", UUID.class)),
                coOwnerRepo.findByAccount(con, accountID),
                reconstructLocation(rs),
                rs.getBigDecimal("balance"),
                rs.getBigDecimal("previous_balance"),
                rs.getInt("multiplier_stage"),
                rs.getInt("remaining_offline_payouts"),
                rs.getString("custom_name")
        );
        bank.addAccount(account);
        cache(account);
        return account;
    }

    private AccountLocation reconstructLocation(ResultSet rs) throws SQLException {
        return AccountLocation.fromDatabase(
                Bukkit.getWorld(rs.getString("world")),
                rs.getInt("x1"),
                rs.getInt("y"),
                rs.getInt("z1"),
                (Integer) rs.getObject("x2"), // Nullable
                (Integer) rs.getObject("z2") // Nullable
        );
    }
    
    private void cache(Account account) {
        accountsById.put(account.getID(), account);
        for (Block block : account.getLocation())
            accountsByBlock.put(block, account);
    }
    
    private void uncache(Account account) {
        accountsById.invalidate(account.getID());
        for (Block block : account.getLocation())
            accountsByBlock.invalidate(block);
    }

}
