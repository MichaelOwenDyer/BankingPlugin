package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
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

public class BankListGUI extends MultiPageGUI<Bank> {

    public BankListGUI(Player player, BankingPlugin plugin) {
        super(plugin, player);
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.sync(plugin.getBankService()::count);
    }
    
    @Override
    Promise<List<Bank>> fetchItems(int offset, int limit) {
        return plugin.getBankService().findAll(offset, limit);
    }
    
    @Override
    String getTitle() {
        return "Bank List";
    }

    @Override
    ItemStack createItem(Bank bank) {
        String lore = bank.getOwner() == null ? "Public" : "Owned by " + bank.getOwner().getName();
        return head(bank.getOwner(), bank.getColorizedName(), lore);
    }
    
    @Override
    void click(Bank bank, ClickType click) {
        child(new BankGUI(plugin, player, bank)).open();
    }
    
    @Override
    List<MenuItemFilter<? super Bank>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Admin Banks", Bank::isAdminBank),
                MenuItemFilter.of("Player Banks", Bank::isPlayerBank)
        );
    }

    @Override
    List<MenuItemSorter<? super Bank>> getSorters() {
        Comparator<Bank> BY_VALUE = Comparator.comparing(Bank::getTotalValue);
        Comparator<Bank> BY_OWNER_NAME = Comparator.comparing(bank -> bank.isPlayerBank() ? bank.getOwner().getName() : "");
        Comparator<Bank> BY_ACCOUNTS = Comparator.comparing(Bank::getNumberOfAccounts);
        return Arrays.asList(
                MenuItemSorter.of("Largest Value", BY_VALUE.reversed()),
                MenuItemSorter.of("Smallest Value", BY_VALUE),
                MenuItemSorter.of("Owner Name A-Z", BY_OWNER_NAME),
                MenuItemSorter.of("Owner Name Z-A", BY_OWNER_NAME.reversed()),
                MenuItemSorter.of("Most Accounts", BY_ACCOUNTS.reversed()),
                MenuItemSorter.of("Fewest Accounts", BY_ACCOUNTS)
        );
    }

}
