package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Ownable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

public abstract class SinglePageGui<T extends Ownable> extends Gui<T> {

    final T guiSubject;
    Menu menu;

    SinglePageGui(T guiSubject) {
        this.guiSubject = guiSubject;
    }

    @Override
    void open(boolean initialize) {
        subscribe(guiSubject);
        if (initialize) {
            initializeMenu();
            setCloseHandler((player, info) -> {
                OPEN_PREVIOUS.close(player, info);
                unsubscribe(guiSubject);
            });
            shortenGuiChain();
        }
        if (menu == null)
            return;
        update();
        menu.open(viewer);
    }

    @Override
    public void update() {
        if (!isInForeground())
            return;
        evaluateClearance(viewer);
        for (int i = 0; i < menu.getDimensions().getArea(); i++) {
            menu.getSlot(i).setItem(createSlotItem(i));
            menu.getSlot(i).setClickHandler(createClickHandler(i));
        }
    }

    @Override
    void close(Player player) {
        prevGui = null;
        menu.close(player);
    }

    abstract void evaluateClearance(Player player);

    abstract ItemStack createSlotItem(int i);

    abstract Slot.ClickHandler createClickHandler(int i);

}
