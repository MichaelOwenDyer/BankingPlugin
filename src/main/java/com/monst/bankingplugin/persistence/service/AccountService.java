package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.persistence.repository.AccountRepository;
import com.monst.bankingplugin.util.Callback;
import com.monst.bankingplugin.util.Utils;
import jakarta.persistence.EntityManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccountService extends EntityService<Account, AccountRepository> {

    public AccountService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new AccountRepository(emf));
        initializeAll();
    }

    /**
     * Initializes all accounts in the database, first by appraising them, then by updating their titles.
     */
    public void initializeAll() {
        for (Account account : findAll()) {
            appraise(account);
            account.updateChestTitle();
        }
    }

    /**
     * Resets all account chest titles.
     */
    public void resetAllChestTitles() {
        for (Account account : findAll())
            account.resetChestTitle();
    }

    /**
     * @return the number of accounts in the database
     */
    public int count() {
        return repo.count();
    }

    /**
     * @param id the ID of the account to find
     * @return the account with the given ID, if it exists
     */
    public Account findByID(int id) {
        return repo.findByID(id);
    }

    /**
     * @param player the owner of the accounts
     * @return the number of accounts in the database owned by the given player
     */
    public int countByOwner(OfflinePlayer player) {
        return repo.countByOwner(player);
    }

    /**
     * Finds the accounts owned by the given players.
     * @param players the owners of the accounts
     * @return the accounts owned by the given players
     */
    public List<Account> findByOwnerIn(Set<OfflinePlayer> players) {
        return repo.findByOwnerIn(players);
    }

    /**
     * Finds the accounts owned by the given players. Runs asynchronously.
     * @param players the owners of the accounts
     * @param callback the callback to call when the accounts are found
     */
    public void findByOwnerIn(Set<OfflinePlayer> players, Callback<Collection<Account>> callback) {
        async(() -> repo.findByOwnerIn(players), callback);
    }

    /**
     * Finds the accounts that the given player is owner or co-owner of. Runs asynchronously.
     * @param player the trusted player
     * @param callback the callback to call when the accounts are found
     */
    public void findByTrustedPlayer(OfflinePlayer player, Callback<Collection<Account>> callback) {
        async(() -> repo.findByTrustedPlayer(player), callback);
    }

    /**
     * Returns whether the given block is an account.
     * @param block the block to check
     * @return whether the given block is an account
     */
    public boolean isAccount(Block block) {
        return findAt(block) != null;
    }

    /**
     * Returns whether any of the given blocks are accounts.
     * @param blocks the blocks to check
     * @return whether any of the given blocks are accounts
     */
    public boolean isAnyAccount(Block... blocks) {
        return isAnyAccount(Arrays.asList(blocks));
    }

    /**
     * Returns whether any of the given blocks are accounts.
     * @param blocks the blocks to check
     * @return whether any of the given blocks are accounts
     */
    public boolean isAnyAccount(Collection<Block> blocks) {
        return !findAt(blocks).isEmpty();
    }

    /**
     * Finds the account located at the given block.
     * If the block is not a chest, null is returned.
     * @param block the block to check
     * @return the Account located at the given block, or null if the block is not a chest or the chest is not an account
     */
    public Account findAt(Block block) {
        if (!Utils.isChest(block))
            return null;
        return repo.findAt(block);
    }

    /**
     * Finds all accounts that are located at the given blocks.
     * Only blocks that are chests will be checked.
     * @param blocks the blocks to check
     * @return a list of accounts that are located at the given blocks
     */
    public List<Account> findAt(Collection<Block> blocks) {
        List<Block> chests = blocks.stream().filter(Utils::isChest).collect(Collectors.toList());
        if (chests.isEmpty())
            return Collections.emptyList();
        if (chests.size() == 1)
            return Collections.singletonList(repo.findAt(chests.get(0)));
        return repo.findAt(chests);
    }

    /**
     * Finds all accounts at the given banks.
     * @param banks the banks to get the accounts from
     * @return the accounts at the given banks
     */
    public List<Account> findByBanks(Collection<Bank> banks) {
        return repo.findByBanks(banks);
    }

    /**
     * Finds all accounts at the given bank owned by the given player.
     * @param bank the bank to get the accounts from
     * @param owner the owner of the accounts
     * @return the accounts at the given bank owned by the given player
     */
    public List<Account> findByBankAndOwner(Bank bank, OfflinePlayer owner) {
        return repo.findByBankAndOwner(bank, owner);
    }

    /**
     * Finds all accounts whose chest cannot be found in the world.
     * @param callback the callback to call when the accounts are found
     */
    public void findAllMissing(Callback<Collection<Account>> callback) {
        async(() -> repo.findAll().stream()
                .filter(account -> !account.getLocation().findChest().isPresent())
                .collect(Collectors.toList()), callback);
    }

    /**
     * Removes the given account from the database.
     * @param account the account to remove
     */
    public void remove(Account account) {
        transaction(em -> em.remove(em.getReference(Account.class, account.getID())));
    }

    /**
     * Removes the given accounts from the database.
     * @param accounts the accounts to remove
     */
    public void removeAll(Collection<Account> accounts) {
        transaction(em -> accounts.stream().map(account -> em.getReference(Account.class, account.getID())).forEach(em::remove));
    }

    /**
     * Appraises the contents of the given account, assuming its chest can be found.
     * The account balance is then updated.
     * @param account the account to appraise
     * @see Account#getContents()
     */
    public void appraise(Account account) {
        EnumMap<Material, Integer> contents = account.getContents();
        BigDecimal balance = contents.entrySet().stream()
                .map(content -> {
                    if (plugin.config().blacklist.contains(content.getKey()))
                        return BigDecimal.ZERO;
                    BigDecimal worth = plugin.getEssentials().getWorth().getPrice(plugin.getEssentials(), new ItemStack(content.getKey()));
                    if (worth == null)
                        return BigDecimal.ZERO;
                    return worth.multiply(BigDecimal.valueOf(content.getValue()));
                })
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        account.setBalance(balance);
    }

}
