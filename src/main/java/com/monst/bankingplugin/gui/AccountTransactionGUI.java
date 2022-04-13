package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountTransaction;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.util.Observable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccountTransactionGUI extends MultiPageGUI<AccountTransaction> {

    private static final int SWITCH_VIEW_SLOT = 31;

    private final SlotSettings switchViewSlot;

    public AccountTransactionGUI(BankingPlugin plugin, Account account) {
        super(plugin, callback -> plugin.getAccountTransactionService().findByAccount(account, callback));
        this.switchViewSlot = createSwitchViewSlot(account);
    }

    @Override
    String getTitle() {
        return "Account Transaction Log";
    }

    @Override
    SlotSettings createSlotSettings(AccountTransaction transaction) {
        Material material = transaction.getDifference().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        ItemStack item = createSlotItem(material, "Transaction #" + transaction.getID(), Arrays.asList(
                transaction.getTimestamp(),
                "Player: " + transaction.getExecutor().getName(),
                "Amount: " + formatAndColorize(transaction.getDifference())
        ));
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    List<MenuItemFilter<? super AccountTransaction>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Deposits", t -> t.getDifference().signum() > 0),
                MenuItemFilter.of("Withdrawals", t -> t.getDifference().signum() < 0)
        );
    }

    @Override
    List<MenuItemSorter<? super AccountTransaction>> getSorters() {
        Comparator<AccountTransaction> BY_TIME = Comparator.comparing(FinancialStatement::getInstant);
        Comparator<AccountTransaction> BY_AMOUNT = Comparator.comparing(t -> t.getDifference().abs());
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
    void modify(Menu page) {
        page.getSlot(SWITCH_VIEW_SLOT).setSettings(switchViewSlot);
    }

    private SlotSettings createSwitchViewSlot(Account account) {
        ItemStack item = createSlotItem(
                Material.BOOK,
                "Account Interest Log",
                Collections.singletonList("Click to view the interest log.")
        );
        return SlotSettings.builder()
                .itemTemplate(new StaticItemTemplate(item))
                .clickHandler((player, info) -> new AccountInterestGUI(plugin, account).setParentGUI(parentGUI).open(player))
                .build();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_TRANSACTION_LOG;
    }

    @Override
    Observable getSubject() {
        return plugin.getAccountTransactionService();
    }

}
