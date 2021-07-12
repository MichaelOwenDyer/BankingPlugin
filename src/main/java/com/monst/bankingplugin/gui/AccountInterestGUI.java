package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.sql.logging.AccountInterest;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import java.util.*;
import java.util.function.Predicate;

public class AccountInterestGUI extends HistoryGUI<AccountInterest> {

    private static final int SWITCH_VIEW_SLOT = 31;

    private static final Predicate<AccountInterest> HAS_LOW_BALANCE_FEE = i -> i.getLowBalanceFee().signum() > 0;
    private static final List<MenuItemFilter<? super AccountInterest>> FILTERS = Arrays.asList(
            MenuItemFilter.of("Has Low Balance Fee", HAS_LOW_BALANCE_FEE),
            MenuItemFilter.of("Has No Low Balance Fee", HAS_LOW_BALANCE_FEE.negate())
    );

    private static final Comparator<AccountInterest> BY_TOTAL_VALUE = Comparator.comparing(AccountInterest::getFinalPayment);
    private static final List<MenuItemSorter<? super AccountInterest>> SORTERS = Arrays.asList(
            MenuItemSorter.of("Largest Value", BY_TOTAL_VALUE.reversed()),
            MenuItemSorter.of("Smallest Value", BY_TOTAL_VALUE)
    );

    private final SlotSettings switchViewSlot;

    AccountInterestGUI(Account account) {
        super(callback -> DATABASE.getInterestPaymentsAtAccount(account, callback), FILTERS, SORTERS);
        this.switchViewSlot = createSwitchViewSlot(account);
    }

    @Override
    String getTitle() {
        return "Account Interest Log";
    }

    @Override
    SlotSettings createSlotSettings(AccountInterest interest) {
        Material material = interest.getFinalPayment().signum() >= 0 ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        List<String> lore = new ArrayList<>();
        lore.add(interest.getTimeFormatted());
        lore.add("Interest: " + ChatColor.GREEN + Utils.format(interest.getInterest()));
        if (interest.getLowBalanceFee().signum() > 0) {
            lore.add("Low Balance Fee: " + ChatColor.RED + Utils.format(interest.getLowBalanceFee()));
            lore.add("Final Amount: " + Utils.formatAndColorize(interest.getFinalPayment()));
        }
        ItemStack item = createSlotItem(material, "Interest #" + interest.getID(), lore);
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    void modify(Menu page) {
        page.getSlot(SWITCH_VIEW_SLOT).setSettings(switchViewSlot);
    }

    private SlotSettings createSwitchViewSlot(Account account) {
        ItemStack item = createSlotItem(
                Material.BOOK,
                "Account Transaction Log",
                Collections.singletonList("Click to view the transaction log.")
        );
        return SlotSettings.builder()
                .itemTemplate(new StaticItemTemplate(item))
                .clickHandler((player, info) -> new AccountTransactionGUI(account).setParentGUI(parentGUI).open(player))
                .build();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_INTEREST_LOG;
    }

}
