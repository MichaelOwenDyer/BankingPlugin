package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

public class AccountContentsGui extends Gui<Account> {

    boolean canEdit;

    public AccountContentsGui(Account account) {
        super(BankingPlugin.getInstance(), account);
    }

    @Override
    Menu getMenu() {
        return ChestMenu.builder(guiSubject.getChestSize() * 3).title(guiSubject.getColorizedName()).build();
    }

    @Override
    void evaluateClearance(Player player) {
        canEdit = player.hasPermission(Permissions.ACCOUNT_EDIT_OTHER);
    }

    @Override
    ItemStack createSlotItem(int i) {
        return guiSubject.getInventoryHolder().getInventory().getItem(i);
    }

    @Override
    Slot.ClickHandler createClickHandler(int i) {
        return null;
        /*if (highClearance)
            gui.getSlot(i).setClickOptions(ClickOptions.ALLOW_ALL);
        return (player, info) -> {
            ItemStack item = gui.getSlot(i).getItem(player);
            guiSubject.getInventoryHolder().getInventory().setItem(i, item);
            if (guiSubject.isDoubleChest()) {
                DoubleChest dc = (DoubleChest) guiSubject.getInventoryHolder();
                ((Chest) dc.getRightSide()).update();
                ((Chest) dc.getLeftSide()).update();
            } else {
                Chest chest = (Chest) guiSubject.getInventoryHolder();
                chest.update();
            }
        };*/
    }

    @Override
    void setCloseHandler(Menu.CloseHandler handler) {
        menu.setCloseHandler(handler);
    }

    @Override
    GuiType getType() {
        return GuiType.ACCOUNT_CONTENTS;
    }
}
