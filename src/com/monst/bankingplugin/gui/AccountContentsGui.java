package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.utils.Permissions;
import org.apache.commons.lang.WordUtils;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

public class AccountContentsGui extends Gui<Account> {

    boolean canEdit;

    public AccountContentsGui(Account account) {
        super(account);
    }

    @Override
    void createMenu() {
        menu = ChestMenu.builder(guiSubject.getSize() * 3).title(guiSubject.getColorizedName()).redraw(true).build();
    }

    @Override
    void evaluateClearance(Player player) {
        canEdit = player.hasPermission(Permissions.ACCOUNT_EDIT_OTHER);
    }

    @Override
    ItemStack createSlotItem(int i) {
        return guiSubject.getInventory(false).getItem(i);
    }

    @Override
    Slot.ClickHandler createClickHandler(int i) {
        ItemStack item = guiSubject.getInventory(false).getItem(i);
        if (item != null && item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
            if (im.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                return (player, info) -> new ShulkerContentsGui(guiSubject, shulkerBox).setPrevGui(this).open(player);
            }
        }
        return null;
        /*if (canEdit)
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

    static class ShulkerContentsGui extends AccountContentsGui {

        private final ShulkerBox shulkerBox;

        public ShulkerContentsGui(Account account, ShulkerBox shulkerBox) {
            super(account);
            this.shulkerBox = shulkerBox;
        }

        @Override
        void createMenu() {
            menu = ChestMenu.builder(3).title(
                    shulkerBox.getCustomName() != null
                            ? shulkerBox.getCustomName()
                            : WordUtils.capitalizeFully(shulkerBox.getColor().toString()) + " Shulker Box"
            ).redraw(true).build();
        }

        @Override
        ItemStack createSlotItem(int i) {
            return shulkerBox.getInventory().getItem(i);
        }

        @Override
        Slot.ClickHandler createClickHandler(int i) {
            return null;
        }

    }
}
