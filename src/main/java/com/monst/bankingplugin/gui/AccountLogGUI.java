package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.sql.logging.AccountTransactionReceipt;
import com.monst.bankingplugin.sql.logging.Receipt;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class AccountLogGUI extends MultiPageGUI<Receipt> {

    static final Comparator<Receipt> BY_AMOUNT = Comparator.comparing(r -> r.getAmount().abs());
    static final Comparator<Receipt> BY_TIME = Comparator.comparing(Receipt::getTime);

    static final List<MenuItemSorter<? super Receipt>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Newest", BY_TIME.reversed()),
            MenuItemSorter.of("Oldest", BY_TIME),
            MenuItemSorter.of("Smallest", BY_AMOUNT),
            MenuItemSorter.of("Largest", BY_AMOUNT.reversed())
    );

    static final List<MenuItemFilter<? super Receipt>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Deposits", t -> t.getAmount().signum() > 0),
            MenuItemFilter.of("Withdrawals", t -> t.getAmount().signum() < 0)
    );

    static final Comparator<AccountTransactionReceipt> BY_EXECUTOR = Comparator.comparing(AccountTransactionReceipt::getExecutorName);

    static final List<MenuItemSorter<? super AccountTransactionReceipt>> s1 = Arrays.asList(
            MenuItemSorter.of("Newest", BY_TIME.reversed()),
            MenuItemSorter.of("Oldest", BY_TIME),
            MenuItemSorter.of("Smallest", BY_AMOUNT),
            MenuItemSorter.of("Largest", BY_AMOUNT.reversed()),
            MenuItemSorter.of("Player Name A-Z", BY_EXECUTOR),
            MenuItemSorter.of("Player Name Z-A", BY_EXECUTOR.reversed())
    );

    AccountLogGUI(Supplier<? extends Collection<? extends Receipt>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Account History";
    }

    @Override
    SlotSettings createSlotSettings(Receipt receipt) {
        if (receipt instanceof AccountTransactionReceipt)
            return createSlotSettings((AccountTransactionReceipt) receipt);
    }

    private SlotSettings createSlotSettings(AccountTransactionReceipt transaction) {
        Material material = transaction.getAmount().signum() >= 0 ? Material.LIME_WOOL : Material.RED_WOOL;
        ChatColor color = transaction.getAmount().signum() >= 0 ? ChatColor.GREEN : ChatColor.RED;
        ItemStack item = createSlotItem(material, "Transaction #" + transaction.getID(), Arrays.asList(
                "Player: " + transaction.getExecutorName(),
                "Amount: " + color + Utils.format(transaction.getAmount()),
                "Time: " + transaction.getTimeFormatted()
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Nullable
    @Override
    Observable getSubject() {
        return null;
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_LOG;
    }
}
