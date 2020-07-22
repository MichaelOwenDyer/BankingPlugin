package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.paginate.PaginatedMenuBuilder;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.template.StaticItemTemplate;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Collections;
import java.util.List;

public class AccountListGui extends Gui<Bank> {

    public AccountListGui(Bank bank) {
        guiSubject = bank;
    }

    @Override
    public void open(Player player) {
        getPaginatedMenu().stream().findFirst().ifPresent(menu -> {
            if (prevGui != null)
                menu.setCloseHandler((player1, menu1) -> {
                    prevGui.open(player);
                });
            menu.open(player);
        });
    }

    private List<Menu> getPaginatedMenu() {
        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Accounts at Bank").redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010000010").build();
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(new ItemStack(Material.ARROW))
                .previousButtonSlot(18)
                .nextButton(new ItemStack(Material.ARROW))
                .nextButtonSlot(26);
        for (Account account : guiSubject.getAccounts()) {
            ItemStack item = Gui.createSlotItem(Material.CHEST, account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(null).open(player);
            builder.addItem(SlotSettings.builder().clickOptions(ClickOptions.DENY_ALL).itemTemplate(template).clickHandler(clickHandler).build());
        }
        return builder.build();
    }

    @Override
    Menu getMenu() {
        return null;
    }

    @Override
    boolean getClearance(Player player) {
        return false;
    }

    @Override
    ItemStack createSlotItem(int i) {
        return null;
    }

    @Override
    Slot.ClickHandler createClickHandler(int i) {
        return null;
    }
}
