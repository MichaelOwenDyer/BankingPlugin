package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.AccountTransactionReceipt;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class AccountTransactionGUI extends ReceiptGUI<AccountTransactionReceipt> {

    static final Comparator<AccountTransactionReceipt> BY_EXECUTOR = Comparator.comparing(AccountTransactionReceipt::getExecutorName);

    static final List<MenuItemSorter<? super AccountTransactionReceipt>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Time Descending", BY_TIME.reversed()),
            MenuItemSorter.of("Time Ascending", BY_TIME),
            MenuItemSorter.of("Amount Ascending", BY_AMOUNT),
            MenuItemSorter.of("Amount Descending", BY_AMOUNT.reversed()),
            MenuItemSorter.of("Player Ascending", BY_EXECUTOR),
            MenuItemSorter.of("Player Descending", BY_EXECUTOR.reversed())
    );

    static final List<MenuItemFilter<? super AccountTransactionReceipt>> FILTERS = Collections.emptyList();

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
        ItemStack item = createSlotItem(material, "Transaction", Arrays.asList(
                "Player: " + transaction.getExecutorName(),
                "Amount: " + color + Utils.format(transaction.getAmount()),
                "Time: " + transaction.getTimeFormatted()
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

}
