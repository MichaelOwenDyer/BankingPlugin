package com.monst.bankingplugin.gui;

import org.bukkit.ChatColor;

import java.util.Comparator;

public abstract class MenuItemSorter<T> implements Comparator<T> {

    public abstract String getName();

    public static <T> MenuItemSorter<T> of(String name, Comparator<? super T> comparator) {
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
