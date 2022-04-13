package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.util.Observable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BankListGUI extends MultiPageGUI<Bank> {

    public BankListGUI(BankingPlugin plugin) {
        super(plugin, callback -> plugin.getBankService().findAll(callback));
    }

    @Override
    String getTitle() {
        return "Bank List";
    }

    @Override
    SlotSettings createSlotSettings(Bank bank) {
        ItemStack item = bank.isPlayerBank() ?
                createSlotItem(bank.getOwner(), bank.getColorizedName(), Collections.singletonList("Owner: " + bank.getOwner().getName())) :
                createSlotItem(Material.PLAYER_HEAD, bank.getColorizedName(), Collections.emptyList());
        ItemStackTemplate template = new StaticItemTemplate(item);
        Slot.ClickHandler clickHandler = (player, info) -> new BankGUI(plugin, bank).setParentGUI(this).open(player);
        return SlotSettings.builder().itemTemplate(template).clickHandler(clickHandler).build();
    }

    @Override
    List<MenuItemFilter<? super Bank>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Admin Banks", Bank::isAdminBank),
                MenuItemFilter.of("Player Banks", Bank::isPlayerBank)
        );
    }

    @Override
    List<MenuItemSorter<? super Bank>> getSorters() {
        Comparator<Bank> BY_VALUE = Comparator.comparing(Bank::getTotalValue);
        Comparator<Bank> BY_OWNER_NAME = Comparator.comparing(bank -> bank.isPlayerBank() ? bank.getOwner().getName() : "");
        Comparator<Bank> BY_ACCOUNTS = Comparator.comparing(Bank::getNumberOfAccounts);
        return Arrays.asList(
                MenuItemSorter.of("Largest Value", BY_VALUE.reversed()),
                MenuItemSorter.of("Smallest Value", BY_VALUE),
                MenuItemSorter.of("Owner Name A-Z", BY_OWNER_NAME),
                MenuItemSorter.of("Owner Name Z-A", BY_OWNER_NAME.reversed()),
                MenuItemSorter.of("Most Accounts", BY_ACCOUNTS.reversed()),
                MenuItemSorter.of("Fewest Accounts", BY_ACCOUNTS)
        );
    }

    @Override
    Observable getSubject() {
        return plugin.getBankService();
    }

    @Override
    GUIType getType() {
        return GUIType.BANK_LIST;
    }
}
