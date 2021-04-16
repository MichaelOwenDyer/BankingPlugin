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
    void open(boolean firstTime) {
        subscribe(getSubject());
        if (firstTime) {
            menu = createMenu();
            menu.setCloseHandler(CLOSE_HANDLER);
            shortenGUIChain();
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
        parentGUI = null;
        menu.close(player);
    }

    @Override
    Observable getSubject() {
        return guiSubject;
    }

    abstract Menu createMenu();

    abstract void evaluateClearance(Player player);

    abstract ItemStack createSlotItem(int slot);

    abstract Slot.ClickHandler createClickHandler(int slot);

    static List<String> getMultiplierLore(List<Integer> multipliers, int highlightStage) {

        if (multipliers.isEmpty())
            return Collections.singletonList(ChatColor.GREEN + "1x");

        List<List<Integer>> stackedMultipliers = Utils.stackList(multipliers);

        int stage = -1;
        if (highlightStage != -1)
            for (List<Integer> level : stackedMultipliers) {
                stage++;
                if (highlightStage - level.size() < 0)
                    break;
                else
                    highlightStage -= level.size();
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
            StringBuilder number = new StringBuilder("" + ChatColor.GOLD + (i == stage ? ChatColor.BOLD : ""));

            number.append(" - ").append(stackedMultipliers.get(i).get(0)).append("x" + ChatColor.DARK_GRAY);

            int levelSize = stackedMultipliers.get(i).size();
            if (levelSize > 1) {
                if (stage == -1) {
                    number.append(" (" + ChatColor.GRAY + "x" + ChatColor.AQUA + levelSize + ChatColor.DARK_GRAY + ")");
                } else if (i < stage) {
                    number.append(" (" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                } else if (i > stage) {
                    number.append(" (" + ChatColor.RED + "0" + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                } else {
                    ChatColor color;
                    if (highlightStage * 3 >= levelSize * 2)
                        color = ChatColor.GREEN;
                    else if (highlightStage * 3 >= levelSize)
                        color = ChatColor.GOLD;
                    else
                        color = ChatColor.RED;
                    number.append(" (" + color + highlightStage + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
                }
            }
            lore.add(number.toString());
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
                && getType() == other.getType()
                && Utils.samePlayer(viewer, other.viewer)
                && Objects.equals(getSubject(), other.getSubject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubject(), viewer, inForeground, getType());
    }

}
