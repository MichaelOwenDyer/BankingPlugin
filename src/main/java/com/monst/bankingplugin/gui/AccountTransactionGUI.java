package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.AccountTransactionReceipt;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class AccountTransactionGUI extends AccountLogGUI {

    static final Comparator<AccountTransactionReceipt> BY_EXECUTOR = Comparator.comparing(AccountTransactionReceipt::getExecutorName);

    static final List<MenuItemSorter<? super AccountTransactionReceipt>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Newest", BY_TIME.reversed()),
            MenuItemSorter.of("Oldest", BY_TIME),
            MenuItemSorter.of("Smallest", BY_AMOUNT),
            MenuItemSorter.of("Largest", BY_AMOUNT.reversed()),
            MenuItemSorter.of("Player Name A-Z", BY_EXECUTOR),
            MenuItemSorter.of("Player Name Z-A", BY_EXECUTOR.reversed())
    );

    static final List<MenuItemFilter<? super AccountTransactionReceipt>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Deposits", t -> t.getAmount().signum() > 0),
            MenuItemFilter.of("Withdrawals", t -> t.getAmount().signum() < 0)
    );

    public AccountTransactionGUI(Supplier<List<AccountTransactionReceipt>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Account Transactions";
    }

    @Override
    SlotSettings createSlotSettings(AccountTransactionReceipt transaction) {
        Material material = transaction.getAmount().signum() >= 0 ? Material.LIME_WOOL : Material.RED_WOOL;
        ChatColor color = transaction.getAmount().signum() >= 0 ? ChatColor.GREEN : ChatColor.RED;
        ItemStack item = createSlotItem(material, "Transaction #" + transaction.getID(), Arrays.asList(
                "Player: " + transaction.getExecutorName(),
                "Amount: " + color + Utils.format(transaction.getAmount()),
                "Time: " + transaction.getTimeFormatted()
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_LOG;
    }

}
