package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
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

    private static Material NEXT_BUTTON = Material.ARROW;
    private static Material PREVIOUS_BUTTON = Material.ARROW;

    public AccountListGui(Bank bank) {
        super(bank);
    }

    @Override
    public void open(Player player) {
        getPaginatedMenu().stream().map(menu -> {
            if (prevGui != null) {
                menu.setCloseHandler((player1, menu1) -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            prevGui.open(player);
                        }
                    }.runTaskLater(BankingPlugin.getInstance(), 1);
                });
            }
            return menu;
        }).findFirst().ifPresent(menu -> {
            menu.open(player);
        });
    }

    private List<Menu> getPaginatedMenu() {
        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Account List").redraw(true);
        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
                .pattern("010101010").build();
        PaginatedMenuBuilder builder = PaginatedMenuBuilder.builder(pageTemplate)
                .slots(itemSlots)
                .previousButton(Gui.createSlotItem(NEXT_BUTTON, "Next Page", Collections.emptyList()))
                .previousButtonSlot(18)
                .nextButton(Gui.createSlotItem(PREVIOUS_BUTTON, "Previous Page", Collections.emptyList()))
                .nextButtonSlot(26);
        for (Account account : guiSubject.getAccounts()) {
            ItemStack item = Gui.createSlotItem(Material.CHEST, account.getColorizedName(), Collections.singletonList("Owner: " + account.getOwnerDisplayName()));
            ItemStackTemplate template = new StaticItemTemplate(item);
            Slot.ClickHandler clickHandler = (player, info) -> new AccountGui(account).setPrevGui(this).open(player);
            builder.addItem(SlotSettings.builder().clickOptions(ClickOptions.DENY_ALL).itemTemplate(template).clickHandler(clickHandler).build());
        }
        return builder.build();
    }

    @Override
    Menu getMenu() {
        return null;
    }

    @Override
    void getClearance(Player player) {
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
