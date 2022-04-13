package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.util.Observable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SinglePageGUI<T> extends GUI<T> {

    final T guiSubject;
    Menu menu;

    SinglePageGUI(BankingPlugin plugin, T guiSubject) {
        super(plugin);
        this.guiSubject = guiSubject;
    }

    @Override
    public void open(Player player) {
        subscribe();
        shortenGUIChain();
        menu = createMenu();
        menu.setCloseHandler(CLOSE_HANDLER);
        evaluateClearance(player);
        for (int i = 0; i < menu.getDimensions().getArea(); i++) {
            menu.getSlot(i).setItem(createSlotItem(i));
            menu.getSlot(i).setClickHandler(createClickHandler(i));
        }
        menu.open(player);
    }

    @Override
    void reopen(Player player) {
        evaluateClearance(player);
        if (needsUpdate)
            update();
        menu.open(player);
    }

    @Override
    public void update() {
        if (!isVisible()) {
            needsUpdate = true;
            return;
        }
        needsUpdate = false;
        for (int i = 0; i < menu.getDimensions().getArea(); i++) {
            menu.getSlot(i).setItem(createSlotItem(i));
            menu.getSlot(i).setClickHandler(createClickHandler(i));
        }
    }

    @Override
    void close(Player player) {
        menu.close(player);
    }

    @Override
    Observable getSubject() {
        return guiSubject instanceof Observable ? (Observable) guiSubject : null;
    }

    abstract Menu createMenu();

    void evaluateClearance(Player player) {}

    abstract ItemStack createSlotItem(int slot);

    abstract Slot.ClickHandler createClickHandler(int slot);

    static List<String> getInterestMultiplierLore(List<Integer> interestMultipliers, int highlightStage) {

        if (interestMultipliers.isEmpty())
            return Collections.singletonList(ChatColor.GREEN + "1x");

        List<List<Integer>> collapsedMultipliers = new ArrayList<>();
        collapsedMultipliers.add(new ArrayList<>());
        collapsedMultipliers.get(0).add(interestMultipliers.get(0));
        int level = 0;
        for (int i = 1; i < interestMultipliers.size(); i++) {
            if (interestMultipliers.get(i).equals(collapsedMultipliers.get(level).get(0)))
                collapsedMultipliers.get(level).add(interestMultipliers.get(i));
            else {
                collapsedMultipliers.add(new ArrayList<>());
                collapsedMultipliers.get(++level).add(interestMultipliers.get(i));
            }
        }

        int stage = -1;
        if (highlightStage != -1) {
            for (List<Integer> multiplierGroup : collapsedMultipliers) {
                stage++;
                if (highlightStage < multiplierGroup.size())
                    break;
                highlightStage -= multiplierGroup.size();
            }
            highlightStage++;
        }

        List<String> lore = new ArrayList<>();

        final int listSize = 5;
        int lower = 0;
        int upper = collapsedMultipliers.size();

        if (stage != -1 && collapsedMultipliers.size() > listSize) {
            lower = stage - (listSize / 2);
            upper = stage + (listSize / 2) + 1;
            while (lower < 0) {
                lower++;
                upper++;
            }
            while (upper > collapsedMultipliers.size()) {
                lower--;
                upper--;
            }

            if (lower > 0)
                lore.add("...");
        }

        for (int i = lower; i < upper; i++) {
            ColorStringBuilder line = new ColorStringBuilder();
            if (i == stage)
                line.bold();

            line.gold(" - ", collapsedMultipliers.get(i).get(0), "x");

            int levelSize = collapsedMultipliers.get(i).size();
            if (levelSize > 1) {
                if (stage == -1) {
                    line.darkGray(" (").gray("x").aqua(levelSize).darkGray(")");
                } else if (i < stage) {
                    line.darkGray(" (").green(levelSize).darkGray("/").green(levelSize).darkGray(")");
                } else if (i > stage) {
                    line.darkGray(" (").red("0").darkGray("/").green(levelSize).darkGray(")");
                } else {
                    line.darkGray(" (");
                    if (highlightStage * 3 >= levelSize * 2)
                        line.green(highlightStage); // Over 2/3rds through the group
                    else if (highlightStage * 3 >= levelSize)
                        line.gold(highlightStage); // Between 1/3rd and 2/3rds through the group
                    else
                        line.red(highlightStage); // Below 1/3rd through the group
                    line.darkGray("/").green(levelSize).darkGray(")");
                }
            }
            lore.add(line.toString());
        }
        if (upper < collapsedMultipliers.size())
            lore.add("...");
        return lore;
    }

}
