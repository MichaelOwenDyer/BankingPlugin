package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.util.Callback;
import com.monst.bankingplugin.util.Observable;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.*;
import java.util.function.Consumer;

public class AccountListGUI extends MultiPageGUI<Account> {

    public AccountListGUI(BankingPlugin plugin, Consumer<Callback<Collection<Account>>> source) {
        super(plugin, source);
    }

    public AccountListGUI(BankingPlugin plugin, Bank bank) {
        this(plugin, callback -> callback.onResult(bank.getAccounts()));
    }

    @Override
    String getTitle() {
        return "Account List";
    }

    @Override
    SlotSettings createSlotSettings(Account account) {
        ItemStack item = createSlotItem(account.getOwner(), account.getName(),
                Collections.singletonList("Owner: " + account.getOwner().getName()));
        ItemStackTemplate template = new StaticItemTemplate(item);
        Slot.ClickHandler clickHandler = (player, info) -> new AccountGUI(plugin, account).setParentGUI(this).open(player);
        return SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build();
    }

    @Override
    List<MenuItemFilter<? super Account>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Single Chest Accounts", Account::isSingleChest),
                MenuItemFilter.of("Double Chest Accounts", Account::isDoubleChest)
        );
    }

    @Override
    List<MenuItemSorter<? super Account>> getSorters() {
        Comparator<Account> BY_BALANCE = Comparator.comparing(Account::getBalance);
        Comparator<Account> BY_OWNER_NAME = Comparator.comparing(account -> account.getOwner().getName());
        Comparator<Account> BY_BANK_NAME = Comparator.comparing(account -> account.getBank().getName());
        return Arrays.asList(
                MenuItemSorter.of("Largest Balance", BY_BALANCE.reversed()),
                MenuItemSorter.of("Smallest Balance", BY_BALANCE),
                MenuItemSorter.of("Owner Name A-Z", BY_OWNER_NAME),
                MenuItemSorter.of("Owner Name Z-A", BY_OWNER_NAME.reversed()),
                MenuItemSorter.of("Bank Name A-Z", BY_BANK_NAME),
                MenuItemSorter.of("Bank Name Z-A", BY_BANK_NAME.reversed())
        );
    }

    @Override
    Observable getSubject() {
        return plugin.getAccountService();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_LIST;
    }

}
