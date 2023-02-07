package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AccountInterestGUI extends MultiPageGUI<AccountInterest> {
    
    private static final int SWITCH_VIEW_SLOT = 31;

    private final Account account;

    AccountInterestGUI(BankingPlugin plugin, Player player, Account account) {
        super(plugin, player);
        this.account = account;
        // TODO: Subscribe to account interest service changes
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.sync(() -> plugin.getAccountInterestService().countByAccount(account));
    }
    
    @Override
    Promise<List<AccountInterest>> fetchItems(int offset, int limit) {
        return plugin.getAccountInterestService().findByAccount(account, offset, limit);
    }

    @Override
    String getTitle() {
        return "Account Interest Log";
    }
    
    @Override
    ItemStack createItem(AccountInterest interest) {
        Material material = interest.getFinalPayment().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        List<String> lore = new ArrayList<>();
        lore.add(interest.getTimestamp());
        lore.add("Interest: " + ChatColor.GREEN + format(interest.getInterest()));
        if (interest.getLowBalanceFee().signum() > 0) {
            lore.add("Low Balance Fee: " + ChatColor.RED + format(interest.getLowBalanceFee()));
            lore.add("Final Amount: " + formatAndColorize(interest.getFinalPayment()));
        }
        return GUI.item(material, "Interest #" + interest.getID(), lore);
    }
    
    @Override
    List<MenuItemFilter<? super AccountInterest>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Has Low Balance Fee", i -> i.getLowBalanceFee().signum() > 0),
                MenuItemFilter.of("Has No Low Balance Fee", i -> i.getLowBalanceFee().signum() == 0)
        );
    }

    @Override
    List<MenuItemSorter<? super AccountInterest>> getSorters() {
        Comparator<AccountInterest> BY_TIME = Comparator.comparing(FinancialStatement::getInstant);
        Comparator<AccountInterest> BY_TOTAL_VALUE = Comparator.comparing(AccountInterest::getFinalPayment);
        return Arrays.asList(
                MenuItemSorter.of("Newest", BY_TIME.reversed()),
                MenuItemSorter.of("Oldest", BY_TIME),
                MenuItemSorter.of("Largest Value", BY_TOTAL_VALUE.reversed()),
                MenuItemSorter.of("Smallest Value", BY_TOTAL_VALUE)
        );
    }
    
    @Override
    Map<Integer, ItemStack> createExtraItems() {
        return Collections.singletonMap(SWITCH_VIEW_SLOT, item(
                Material.BOOK,
                "Account Transaction Log",
                "Click to view the transaction log."
        ));
    }
    
    @Override
    public void click(int slot, ClickType clickType) {
        super.click(slot, clickType);
        if (slot == SWITCH_VIEW_SLOT)
            parentGUI.child(new AccountTransactionGUI(plugin, player, account)).open();
    }

}
