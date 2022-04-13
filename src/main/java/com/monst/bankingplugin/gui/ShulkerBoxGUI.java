package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

public class ShulkerBoxGUI extends SinglePageGUI<ShulkerBox> {

    public ShulkerBoxGUI(BankingPlugin plugin, ShulkerBox guiSubject) {
        super(plugin, guiSubject);
    }

    @Override
    Menu createMenu() {
        return ChestMenu.builder(3).title("Shulker Box").redraw(true).build();
//                    shulkerBox.getCustomName() != null ?
//                            shulkerBox.getCustomName() : // TODO: Figure out why always null
//                            WordUtils.capitalizeFully(shulkerBox.getColor().toString())
//                       FIXME: shulkerBox.getColor() throws NullPointerException when Shulker Box default color

    }

    @Override
    ItemStack createSlotItem(int slot) {
        return guiSubject.getInventory().getItem(slot);
    }

    @Override
    Slot.ClickHandler createClickHandler(int slot) {
        return null;
    }

    @Override
    GUIType getType() {
        return GUIType.SHULKER_BOX;
    }

}
