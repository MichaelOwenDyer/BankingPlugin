package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.log.BankIncome;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.gui.option.MenuItemFilter;
import com.monst.bankingplugin.gui.option.MenuItemSorter;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BankIncomeGUI extends MultiPageGUI<BankIncome> {
    
    private final Bank bank;

    public BankIncomeGUI(BankingPlugin plugin, Player player, Bank bank) {
        super(plugin, player);
        this.bank = bank;
    }
    
    @Override
    Promise<Integer> countItems() {
        return Promise.sync(() -> plugin.getBankIncomeService().countByBank(bank));
    }
    
    @Override
    Promise<List<BankIncome>> fetchItems(int offset, int limit) {
        return plugin.getBankIncomeService().findByBank(bank, offset, limit);
    }
    
    @Override
    String getTitle() {
        return "Bank Income Log";
    }
    
    @Override
    ItemStack createItem(BankIncome income) {
        Material icon = income.getNetIncome().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        return GUI.item(icon, "Income #" + income.getID(), Arrays.asList(
                income.getTimestamp(),
                "  Gross Income: " + ChatColor.GREEN + format(income.getRevenue()),
                "+ Fees Received: " + ChatColor.GREEN + format(income.getLowBalanceFees()),
                "- Interest Paid: " + ChatColor.RED + format(income.getInterest()),
                "                 ------------",
                "  Net Profit:    " + formatAndColorize(income.getNetIncome())
        ));
    }
    
    @Override
    List<MenuItemFilter<? super BankIncome>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Profits", p -> p.getNetIncome().signum() >= 0),
                MenuItemFilter.of("Losses", p -> p.getNetIncome().signum() < 0)
        );
    }

    @Override
    List<MenuItemSorter<? super BankIncome>> getSorters() {
        Comparator<BankIncome> BY_TIME = Comparator.comparing(FinancialStatement::getInstant);
        Comparator<BankIncome> BY_PROFIT = Comparator.comparing(BankIncome::getNetIncome);
        Comparator<BankIncome> BY_REVENUE = Comparator.comparing(BankIncome::getRevenue);
        Comparator<BankIncome> BY_INTEREST = Comparator.comparing(BankIncome::getInterest);
        Comparator<BankIncome> BY_LOW_BALANCE_FEES = Comparator.comparing(BankIncome::getLowBalanceFees);
        return Arrays.asList(
                MenuItemSorter.of("Newest", BY_TIME.reversed()),
                MenuItemSorter.of("Oldest", BY_TIME),
                MenuItemSorter.of("Most Profit", BY_PROFIT.reversed()),
                MenuItemSorter.of("Least Profit", BY_PROFIT),
                MenuItemSorter.of("Most Revenue", BY_REVENUE.reversed()),
                MenuItemSorter.of("Least Revenue", BY_REVENUE),
                MenuItemSorter.of("Most Interest", BY_INTEREST.reversed()),
                MenuItemSorter.of("Least Interest", BY_INTEREST),
                MenuItemSorter.of("Most Low Balance Fees", BY_LOW_BALANCE_FEES.reversed()),
                MenuItemSorter.of("Least Low Balance Fees", BY_LOW_BALANCE_FEES)
        );
    }

}
