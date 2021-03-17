package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.*;
import java.util.function.Supplier;

public class AccountListGUI extends MultiPageGUI<Account> {

    private static final Comparator<Account> BY_BALANCE = Comparator.comparing(Account::getBalance);
    private static final Comparator<Account> BY_OWNER_NAME = Comparator.comparing(account -> account.getOwner().getName());
    private static final Comparator<Account> BY_BANK_NAME = Comparator.comparing(account -> account.getBank().getName());

    static final List<MenuItemSorter<? super Account>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Smallest Balance", BY_BALANCE),
            MenuItemSorter.of("Largest Balance", BY_BALANCE.reversed()),
            MenuItemSorter.of("Owner Name A-Z", BY_OWNER_NAME),
            MenuItemSorter.of("Owner Name Z-A", BY_OWNER_NAME.reversed()),
            MenuItemSorter.of("Bank Name A-Z", BY_BANK_NAME),
            MenuItemSorter.of("Bank Name Z-A", BY_BANK_NAME.reversed())
    );
    static final List<MenuItemFilter<? super Account>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Single Chest Accounts", Account::isSingleChest),
            MenuItemFilter.of("Double Chest Accounts", Account::isDoubleChest)
    );

    public AccountListGUI(Supplier<Set<? extends Account>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Account List";
    }

    @Override
    SlotSettings createSlotSettings(Account account) {
        ItemStack item = createSlotItem(account.getOwner(), account.getChestName(),
                Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
        ItemStackTemplate template = new StaticItemTemplate(item);
        Slot.ClickHandler clickHandler = (player, info) -> new AccountGUI(account).setParentGUI(this).open(player);
        return SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build();
    }

    @Override
    Observable getSubject() {
        return BankingPlugin.getInstance().getAccountRepository();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_LIST;
    }

}
