package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.*;
import java.util.function.Supplier;

public class BankListGUI extends MultiPageGUI<Bank> {

    private static final Comparator<Bank> BY_VALUE = Comparator.comparing(Bank::getTotalValue);
    private static final Comparator<Bank> BY_OWNER_NAME = Comparator.comparing(bank -> bank.getOwner() != null ? bank.getOwner().getName() : "");
    private static final Comparator<Bank> BY_ACCOUNTS = Comparator.comparing(bank -> bank.getAccounts().size());

    static final List<MenuItemSorter<Bank>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Total Value Ascending", BY_VALUE),
            MenuItemSorter.of("Total Value Descending", BY_VALUE.reversed()),
            MenuItemSorter.of("Owner Name Ascending", BY_OWNER_NAME),
            MenuItemSorter.of("Owner Name Descending", BY_OWNER_NAME.reversed()),
            MenuItemSorter.of("Accounts Ascending", BY_ACCOUNTS),
            MenuItemSorter.of("Accounts Descending", BY_ACCOUNTS.reversed())
    );

    static final List<MenuItemFilter<Bank>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Admin Banks", Bank::isAdminBank),
            MenuItemFilter.of("Player Banks", Bank::isPlayerBank)
    );

    public BankListGUI(Supplier<Set<? extends Bank>> source) {
        super(source, FILTERS, SORTERS);
    }

    @Override
    String getTitle() {
        return "Bank List";
    }

    @Override
    void addItems(PaginatedMenuBuilder builder) {
        for (Bank bank : getMenuItems()) {
            ItemStack item = bank.isPlayerBank() ?
                    createSlotItem(bank.getOwner(), bank.getColorizedName(), Collections.singletonList("Owner: " + bank.getOwnerDisplayName())) :
                    createSlotItem(Material.PLAYER_HEAD, bank.getColorizedName(), Collections.singletonList("Owner: " + bank.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new BankGUI(bank).setParentGUI(this).open(player);
            builder.addItem(SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build());
        }
    }

    @Override
    Observable getSubject() {
        return plugin.getBankRepository();
    }

    @Override
    GUIType getType() {
        return GUIType.BANK_LIST;
    }
}
