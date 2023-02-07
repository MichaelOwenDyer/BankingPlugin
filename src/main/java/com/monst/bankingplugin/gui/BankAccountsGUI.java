package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class BankAccountsGUI extends MultiPageGUI<Account> {
    
    private final Bank bank;

    BankAccountsGUI(BankingPlugin plugin, Player player, Bank bank) {
        super(plugin, player);
        this.bank = bank;
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.fulfill(bank.getAccounts().size());
    }
    
    @Override
    Promise<List<Account>> fetchItems(int offset, int limit) {
        return Promise.fulfill(bank.getAccounts().stream().skip(offset).limit(limit).collect(Collectors.toList()));
    }
    
    @Override
    String getTitle() {
        return "Accounts of " + bank.getName();
    }
    
    @Override
    ItemStack createItem(Account account) {
        return GUI.head(account.getOwner(), account.getName(), "Owned by " + account.getOwner().getName());
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
