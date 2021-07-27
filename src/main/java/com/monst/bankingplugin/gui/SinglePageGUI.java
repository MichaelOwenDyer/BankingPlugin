package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class SinglePageGUI<T extends BankingEntity> extends GUI<T> {

    final T guiSubject;
    Menu menu;

    SinglePageGUI(T guiSubject) {
        this.guiSubject = guiSubject;
    }

    @Override
    public void open(Player player) {
        subscribe();
        shortenGUIChain();
        menu = createMenu();
        menu.setCloseHandler(CLOSE_HANDLER);
        evaluateClearance(player);
        update();
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
        if (!isInForeground()) {
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
        return guiSubject;
    }

    abstract Menu createMenu();

    void evaluateClearance(Player player) {

    }

    abstract ItemStack createSlotItem(int slot);

    abstract Slot.ClickHandler createClickHandler(int slot);

    static List<String> getMultiplierLore(List<Integer> multipliers, int highlightStage) {

        if (multipliers.isEmpty())
            return Collections.singletonList(ChatColor.GREEN + "1x");

        List<List<Integer>> stackedMultipliers = Utils.stackList(multipliers);

        int stage = -1;
        if (highlightStage != -1) {
            for (List<Integer> level : stackedMultipliers) {
                stage++;
                if (highlightStage < level.size())
                    break;
                else
                    highlightStage -= level.size();
            }
            highlightStage++;
        }

        List<String> lore = new ArrayList<>();

        final int listSize = 5;
        int lower = 0;
        int upper = stackedMultipliers.size();

        if (stage != -1 && stackedMultipliers.size() > listSize) {
            lower = stage - (listSize / 2);
            upper = stage + (listSize / 2) + 1;
            while (lower < 0) {
                lower++;
                upper++;
            }
            while (upper > stackedMultipliers.size()) {
                lower--;
                upper--;
            }

            if (lower > 0)
                lore.add("...");
        }

        for (int i = lower; i < upper; i++) {
            StringBuilder line = new StringBuilder("" + ChatColor.GOLD + (i == stage ? ChatColor.BOLD : ""));

            line.append(" - ").append(stackedMultipliers.get(i).get(0)).append("x" + ChatColor.DARK_GRAY);

            int levelSize = stackedMultipliers.get(i).size();
            if (levelSize > 1) {
                if (stage == -1) {
                    line.append(" (" + ChatColor.GRAY + "x" + ChatColor.AQUA + levelSize + ChatColor.DARK_GRAY + ")");
                } else if (i < stage) {
                    line.append(" (" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                } else if (i > stage) {
                    line.append(" (" + ChatColor.RED + "0" + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                } else {
                    ChatColor color;
                    if (highlightStage * 3 >= levelSize * 2)
                        color = ChatColor.GREEN; // Over 2/3rds through the group
                    else if (highlightStage * 3 >= levelSize)
                        color = ChatColor.GOLD; // Between 1/3rd and 2/3rds through the group
                    else
                        color = ChatColor.RED; // Below 1/3rd through the group
                    line.append(" (" + color + highlightStage + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                }
            }
            lore.add(line.toString());
        }
        if (upper < stackedMultipliers.size())
            lore.add("...");
        return lore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SinglePageGUI<?> other = (SinglePageGUI<?>) o;
        return inForeground == other.inForeground
                && getType() == other.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(inForeground, getType());
    }

}
