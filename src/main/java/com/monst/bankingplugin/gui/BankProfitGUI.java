package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.BankProfit;
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

public class BankProfitGUI extends HistoryGUI<BankProfit> {

    private static final Predicate<BankProfit> IS_PROFIT = p -> p.getProfit().signum() >= 0;
    private static final Predicate<BankProfit> IS_LOSS = p -> p.getProfit().signum() < 0;
    private static final List<MenuItemFilter<? super BankProfit>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Profits", IS_PROFIT),
            MenuItemFilter.of("Losses", IS_LOSS)
    );

    private static final Comparator<BankProfit> BY_PROFIT = Comparator.comparing(BankProfit::getProfit);
    private static final Comparator<BankProfit> BY_REVENUE = Comparator.comparing(BankProfit::getRevenue);
    private static final Comparator<BankProfit> BY_INTEREST = Comparator.comparing(BankProfit::getInterest);
    private static final Comparator<BankProfit> BY_LOW_BALANCE_FEES = Comparator.comparing(BankProfit::getLowBalanceFees);
    private static final List<MenuItemSorter<? super BankProfit>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Most Profit", BY_PROFIT.reversed()),
            MenuItemSorter.of("Least Profit", BY_PROFIT),
            MenuItemSorter.of("Most Revenue", BY_REVENUE.reversed()),
            MenuItemSorter.of("Least Revenue", BY_REVENUE),
            MenuItemSorter.of("Most Interest", BY_INTEREST.reversed()),
            MenuItemSorter.of("Least Interest", BY_INTEREST),
            MenuItemSorter.of("Most Low Balance Fees", BY_LOW_BALANCE_FEES.reversed()),
            MenuItemSorter.of("Least Low Balance Fees", BY_LOW_BALANCE_FEES)
    );

    public BankProfitGUI(Supplier<List<BankProfit>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Bank Revenue Log";
    }

    @Override
    SlotSettings createSlotSettings(BankProfit profit) {
        Material material = profit.getProfit().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        ItemStack item = createSlotItem(material, "Interest #" + profit.getID(), Arrays.asList(
                profit.getTimeFormatted(),
                "Revenue: " + ChatColor.GREEN + Utils.format(profit.getRevenue()),
                "Account Interest: " + ChatColor.RED + Utils.format(profit.getInterest()),
                "Low Balance Fees: " + ChatColor.GREEN + Utils.format(profit.getLowBalanceFees()),
                "Total Profit: " + Utils.formatAndColorize(profit.getProfit())
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    GUIType getType() {
        return GUIType.BANK_PROFIT_LOG;
    }

}
