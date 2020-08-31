package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Ownable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SinglePageGui<T extends Ownable> extends Gui<T> {

    final T guiSubject;
    Menu menu;

    public static void updateGuis(Ownable ownable) {
        GuiTracker.get(ownable).forEach(SinglePageGui::update);
    }

    public static void updateGuis() {
        GuiTracker.get().forEach(SinglePageGui::update);
    }

    SinglePageGui(T guiSubject) {
        this.guiSubject = guiSubject;
    }

    @Override
    void open(boolean initialize) {
        GuiTracker.add(guiSubject, this);
        if (initialize) {
            initializeMenu();
            setCloseHandler((player, menu) -> {
                GuiTracker.remove(guiSubject, this);
                OPEN_PREVIOUS.close(player, menu);
            });
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

    abstract void evaluateClearance(Player player);

    abstract ItemStack createSlotItem(int i);

    abstract Slot.ClickHandler createClickHandler(int i);

    private static class GuiTracker {

        private static final Map<Ownable, Set<SinglePageGui<?>>> openGuis = new HashMap<>();

        private static void add(Ownable ownable, SinglePageGui<?> gui) {
            openGuis.putIfAbsent(ownable, new HashSet<>());
            openGuis.get(ownable).add(gui);
        }

        private static void remove(Ownable ownable, SinglePageGui<?> gui) {
            openGuis.get(ownable).remove(gui);
            if (openGuis.get(ownable).isEmpty())
                openGuis.remove(ownable);
        }

        private static Set<SinglePageGui<?>> get(Ownable ownable) {
            return openGuis.getOrDefault(ownable, new HashSet<>());
        }

        private static Set<SinglePageGui<?>> get() {
            return openGuis.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        }

    }

}
