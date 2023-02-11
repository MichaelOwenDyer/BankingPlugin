package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AccountListGUI extends MultiPageGUI<Account> {
    
    private final Set<OfflinePlayer> owners;

    public AccountListGUI(BankingPlugin plugin, Player player, Set<OfflinePlayer> owners) {
        super(plugin, player);
        this.owners = owners;
        // TODO: Subscribe to account repository changes
    }
    
    @Override
    Promise<Integer> countItems() {
        if (owners.isEmpty())
            return plugin.getAccountService().countByTrustedPlayer(player);
        if (Permissions.ACCOUNT_LIST_OTHER.ownedBy(player))
            return plugin.getAccountService().countByOwners(owners);
        if (owners.contains(player))
            return plugin.getAccountService().countByOwners(Collections.singleton(player));
        return Promise.fulfill(0);
    }
    
    @Override
    Promise<List<Account>> fetchItems(int offset, int limit) {
        if (owners.isEmpty())
            return plugin.getAccountService().findByTrustedPlayer(player, offset, limit);
        if (Permissions.ACCOUNT_LIST_OTHER.ownedBy(player))
            return plugin.getAccountService().findByOwners(owners, offset, limit);
        if (owners.contains(player))
            return plugin.getAccountService().findByOwners(Collections.singleton(player), offset, limit);
        return Promise.fulfill(Collections.emptyList());
    }
    
    @Override
    String getTitle() {
        return "Account List";
    }
    
    @Override
    ItemStack createItem(Account account) {
        return GUI.head(account.getOwner(), account.getName(), "Owner: " + account.getOwner().getName());
    }
    
    @Override
    void click(Account account, ClickType click) {
        child(new AccountGUI(plugin, player, account)).open();
    }
    
    @Override
    List<MenuItemFilter<? super Account>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Single Chest Accounts", Account::isSingleChest),
                MenuItemFilter.of("Double Chest Accounts", Account::isDoubleChest)
        );
    }

    @Override
    List<MenuItemSorter<? super Account>> getSorters() {
        Comparator<Account> BY_BALANCE = Comparator.comparing(Account::getBalance);
        Comparator<Account> BY_OWNER_NAME = Comparator.comparing(account -> account.getOwner().getName());
        Comparator<Account> BY_BANK_NAME = Comparator.comparing(account -> account.getBank().getName());
        return Arrays.asList(
                MenuItemSorter.of("Largest Balance", BY_BALANCE.reversed()),
                MenuItemSorter.of("Smallest Balance", BY_BALANCE),
                MenuItemSorter.of("Owner Name A-Z", BY_OWNER_NAME),
                MenuItemSorter.of("Owner Name Z-A", BY_OWNER_NAME.reversed()),
                MenuItemSorter.of("Bank Name A-Z", BY_BANK_NAME),
                MenuItemSorter.of("Bank Name Z-A", BY_BANK_NAME.reversed())
        );
    }
    
}
