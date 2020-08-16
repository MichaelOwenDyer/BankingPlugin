package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.utils.Ownable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

abstract class SinglePageGui<T extends Ownable> extends Gui<T> {

    Menu menu;

    SinglePageGui(T guiSubject) {
        super(guiSubject);
    }

    void open(Player player, boolean update) {
        if (update) {
            initializeMenu();
            setCloseHandler(OPEN_PREVIOUS);
            shortenGuiChain();
        }
        evaluateClearance(player);
        for (int i = 0; i < menu.getDimensions().getArea(); i++) {
            menu.getSlot(i).setItem(createSlotItem(i));
            menu.getSlot(i).setClickHandler(createClickHandler(i));
        }
        menu.open(player);
    }

    abstract void evaluateClearance(Player player);

    abstract ItemStack createSlotItem(int i);

    abstract Slot.ClickHandler createClickHandler(int i);

}
