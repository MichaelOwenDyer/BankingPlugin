package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Ownable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class SinglePageGui<T extends Ownable> extends Gui<T> {

    final T guiSubject;
    Menu menu;

    static Map<Ownable, Set<SinglePageGui<?>>> openGuis = new HashMap<>();

    public static void updateGuis(Ownable ownable) {
        if (openGuis.containsKey(ownable))
            openGuis.get(ownable).forEach(SinglePageGui::update);
    }

    final Menu.CloseHandler REMOVE_AND_OPEN_PREVIOUS = (player, menu) -> {
        openGuis.get(getGuiSubject()).remove(this);
        if (openGuis.get(getGuiSubject()).isEmpty())
            openGuis.remove(getGuiSubject());
        OPEN_PREVIOUS.close(player, menu);
    };

    SinglePageGui(T guiSubject) {
        this.guiSubject = guiSubject;
    }

    @Override
    void open(boolean initialize) {
        openGuis.putIfAbsent(guiSubject, new HashSet<>());
        openGuis.get(guiSubject).add(this);
        if (initialize) {
            initializeMenu();
            setCloseHandler(REMOVE_AND_OPEN_PREVIOUS);
            shortenGuiChain();
        }
        update();
        menu.open(viewer);
    }

    void update() {
        if (!isInForeground())
            return;
        evaluateClearance(viewer);
        for (int i = 0; i < menu.getDimensions().getArea(); i++) {
            menu.getSlot(i).setItem(createSlotItem(i));
            menu.getSlot(i).setClickHandler(createClickHandler(i));
        }
    }

    void close(Player player) {
        prevGui = null;
        menu.close(player);
    }

    T getGuiSubject() {
        return guiSubject;
    }

    abstract void evaluateClearance(Player player);

    abstract ItemStack createSlotItem(int i);

    abstract Slot.ClickHandler createClickHandler(int i);

}
