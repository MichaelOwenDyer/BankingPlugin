package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.BankIncome;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BankIncomeGUI extends HistoryGUI<BankIncome> {

    private static final Predicate<BankIncome> IS_PROFIT = p -> p.getProfit().signum() >= 0;
    private static final Predicate<BankIncome> IS_LOSS = p -> p.getProfit().signum() < 0;
    private static final List<MenuItemFilter<? super BankIncome>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Profits", IS_PROFIT),
            MenuItemFilter.of("Losses", IS_LOSS)
    );

    private static final Comparator<BankIncome> BY_PROFIT = Comparator.comparing(BankIncome::getProfit);
    private static final Comparator<BankIncome> BY_REVENUE = Comparator.comparing(BankIncome::getRevenue);
    private static final Comparator<BankIncome> BY_INTEREST = Comparator.comparing(BankIncome::getInterest);
    private static final Comparator<BankIncome> BY_LOW_BALANCE_FEES = Comparator.comparing(BankIncome::getLowBalanceFees);
    private static final List<MenuItemSorter<? super BankIncome>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Most Profit", BY_PROFIT.reversed()),
            MenuItemSorter.of("Least Profit", BY_PROFIT),
            MenuItemSorter.of("Most Revenue", BY_REVENUE.reversed()),
            MenuItemSorter.of("Least Revenue", BY_REVENUE),
            MenuItemSorter.of("Most Interest", BY_INTEREST.reversed()),
            MenuItemSorter.of("Least Interest", BY_INTEREST),
            MenuItemSorter.of("Most Low Balance Fees", BY_LOW_BALANCE_FEES.reversed()),
            MenuItemSorter.of("Least Low Balance Fees", BY_LOW_BALANCE_FEES)
    );

    public BankIncomeGUI(Supplier<List<BankIncome>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Bank Income Log";
    }

    @Override
    SlotSettings createSlotSettings(BankIncome income) {
        Material material = income.getProfit().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        ItemStack item = createSlotItem(material, "Interest #" + income.getID(), Arrays.asList(
                income.getTimeFormatted(),
                "Revenue: " + ChatColor.GREEN + Utils.format(income.getRevenue()),
                "Account Interest: " + ChatColor.RED + Utils.format(income.getInterest()),
                "Low Balance Fees: " + ChatColor.GREEN + Utils.format(income.getLowBalanceFees()),
                "Total Profit: " + Utils.formatAndColorize(income.getProfit())
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    GUIType getType() {
        return GUIType.BANK_INCOME_LOG;
    }

}
