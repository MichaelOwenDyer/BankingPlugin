package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.log.AccountInterest;
import com.monst.bankingplugin.entity.log.FinancialStatement;
import com.monst.bankingplugin.util.Observable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.StaticItemTemplate;

import javax.annotation.Nullable;
import java.util.*;

public class AccountInterestGUI extends MultiPageGUI<AccountInterest> {

    private static final int SWITCH_VIEW_SLOT = 31;

    private final SlotSettings switchViewSlot;

    AccountInterestGUI(BankingPlugin plugin, Account account) {
        super(plugin, callback -> plugin.getAccountInterestService().findByAccount(account, callback));
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
        lore.add(interest.getTimestamp());
        lore.add("Interest: " + ChatColor.GREEN + format(interest.getInterest()));
        if (interest.getLowBalanceFee().signum() > 0) {
            lore.add("Low Balance Fee: " + ChatColor.RED + format(interest.getLowBalanceFee()));
            lore.add("Final Amount: " + formatAndColorize(interest.getFinalPayment()));
        }
        ItemStack item = createSlotItem(material, "Interest #" + interest.getID(), lore);
        return SlotSettings.builder().itemTemplate(new StaticItemTemplate(item)).build();
    }

    @Override
    List<MenuItemFilter<? super AccountInterest>> getFilters() {
        return Arrays.asList(
                MenuItemFilter.of("Has Low Balance Fee", i -> i.getLowBalanceFee().signum() > 0),
                MenuItemFilter.of("Has No Low Balance Fee", i -> i.getLowBalanceFee().signum() == 0)
        );
    }

    @Override
    List<MenuItemSorter<? super AccountInterest>> getSorters() {
        Comparator<AccountInterest> BY_TIME = Comparator.comparing(FinancialStatement::getInstant);
        Comparator<AccountInterest> BY_TOTAL_VALUE = Comparator.comparing(AccountInterest::getFinalPayment);
        return Arrays.asList(
                MenuItemSorter.of("Newest", BY_TIME.reversed()),
                MenuItemSorter.of("Oldest", BY_TIME),
                MenuItemSorter.of("Largest Value", BY_TOTAL_VALUE.reversed()),
                MenuItemSorter.of("Smallest Value", BY_TOTAL_VALUE)
        );
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
                .clickHandler((player, info) -> new AccountTransactionGUI(plugin, account).setParentGUI(parentGUI).open(player))
                .build();
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_INTEREST_LOG;
    }

    @Nullable
    @Override
    Observable getSubject() {
        return plugin.getAccountInterestService();
    }

}
