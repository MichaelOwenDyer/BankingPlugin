package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AccountTransactionGUI extends MultiPageGUI<AccountTransaction> {

    private static final int SWITCH_VIEW_SLOT = 31;

    private final Account account;

    public AccountTransactionGUI(BankingPlugin plugin, Player player, Account account) {
        super(plugin, player);
        this.account = account;
        // TODO: Subscribe to account transaction service changes
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.sync(() -> plugin.getAccountTransactionService().countByAccount(account));
    }
    
    @Override
    Promise<List<AccountTransaction>> fetchItems(int offset, int limit) {
        return plugin.getAccountTransactionService().findByAccount(account, offset, limit);
    }
    
    @Override
    String getTitle() {
        return "Account Transaction Log";
    }
    
    @Override
    ItemStack createItem(AccountTransaction transaction) {
        Material material = transaction.getAmount().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        return item(material, "Transaction #" + transaction.getID(), Arrays.asList(
                transaction.getTimestamp(),
                "Player: " + transaction.getExecutor().getName(),
                "Amount: " + formatAndColorize(transaction.getAmount())
        ));
    }
    
    @Override
    List<MenuItemFilter<? super AccountTransaction>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Deposits", t -> t.getAmount().signum() > 0),
                MenuItemFilter.of("Withdrawals", t -> t.getAmount().signum() < 0)
        );
    }

    @Override
    List<MenuItemSorter<? super AccountTransaction>> getSorters() {
        Comparator<AccountTransaction> BY_TIME = Comparator.comparing(FinancialStatement::getInstant);
        Comparator<AccountTransaction> BY_AMOUNT = Comparator.comparing(t -> t.getAmount().abs());
        Comparator<AccountTransaction> BY_EXECUTOR = Comparator.comparing(t -> t.getExecutor().getName());
        return Arrays.asList(
                MenuItemSorter.of("Newest", BY_TIME.reversed()),
                MenuItemSorter.of("Oldest", BY_TIME),
                MenuItemSorter.of("Largest Amount", BY_AMOUNT.reversed()),
                MenuItemSorter.of("Smallest Amount", BY_AMOUNT),
                MenuItemSorter.of("Player Name A-Z", BY_EXECUTOR),
                MenuItemSorter.of("Player Name Z-A", BY_EXECUTOR.reversed())
        );
    }
    
    @Override
    Map<Integer, ItemStack> createExtraItems() {
        return Collections.singletonMap(SWITCH_VIEW_SLOT, item(
                Material.BOOK,
                "Account Interest Log",
                "Click to view the interest log."
        ));
    }
    
    @Override
    public void click(int slot, ClickType clickType) {
        super.click(slot, clickType);
        if (slot == SWITCH_VIEW_SLOT)
            parentGUI.child(new AccountInterestGUI(plugin, player, account)).open();
    }

}
