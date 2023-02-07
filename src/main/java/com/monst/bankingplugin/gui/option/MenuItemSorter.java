package com.monst.bankingplugin.gui.option;

import org.bukkit.ChatColor;

import java.util.Comparator;

public interface MenuItemSorter<T> extends MenuItemController, Comparator<T> {

    static <T> MenuItemSorter<T> unsorted() {
        return of(ChatColor.GRAY + "Unsorted", (t1, t2) -> 0);
    }

    static <T> MenuItemSorter<T> of(String name, Comparator<? super T> comparator) {
        return new MenuItemSorter<T>() {
            @Override
            public String getName() {
                return ChatColor.GOLD + name;
            }

            @Override
            public int compare(T o1, T o2) {
                return comparator.compare(o1, o2);
            }
        };
    }

}
