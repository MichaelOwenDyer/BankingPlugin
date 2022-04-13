package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

public class AccountContentsGUI extends SinglePageGUI<Account> {

    private final InventoryHolder inventoryHolder;
    // boolean canEdit; TODO: Implement remote editing

    public AccountContentsGUI(BankingPlugin plugin, Account account) throws IllegalArgumentException {
        super(plugin, account);
        this.inventoryHolder = account.getLocation().findChest().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    Menu createMenu() {
        return ChestMenu.builder(guiSubject.getSize() * 3).title(guiSubject.getName()).redraw(true).build();
    }

//    @Override
//    void evaluateClearance(Player player) {
//        canEdit = Permission.ACCOUNT_EDIT_OTHER.ownedBy(player);
//    }

    @Override
    ItemStack createSlotItem(int slot) {
        return inventoryHolder.getInventory().getItem(slot);
    }

    @Override
    Slot.ClickHandler createClickHandler(int slot) {
        ItemStack item = inventoryHolder.getInventory().getItem(slot);
        if (item != null && item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
            if (im.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                return (player, info) -> new ShulkerBoxGUI(plugin, shulkerBox).setParentGUI(this).open(player);
            }
        }
        return null;
//        if (canEdit)
//            gui.getSlot(i).setClickOptions(ClickOptions.ALLOW_ALL);
//        return (player, info) -> {
//            ItemStack item = gui.getSlot(i).getItem(player);
//            guiSubject.getInventoryHolder().getInventory().setItem(i, item);
//            if (guiSubject.isDoubleChest()) {
//                DoubleChest dc = (DoubleChest) guiSubject.getInventoryHolder();
//                ((Chest) dc.getRightSide()).update();
//                ((Chest) dc.getLeftSide()).update();
//            } else {
//                Chest chest = (Chest) guiSubject.getInventoryHolder();
//                chest.update();
//            }
//        };
    }

    @Override
    GUIType getType() {
        return GUIType.ACCOUNT_CONTENTS;
    }

}
