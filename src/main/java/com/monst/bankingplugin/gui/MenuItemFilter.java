package com.monst.bankingplugin.gui;

import org.bukkit.ChatColor;

import java.util.function.Predicate;

public interface MenuItemFilter<T> extends MenuItemController, Predicate<T> {

    static <T> MenuItemFilter<T> all() {
        return of(ChatColor.GRAY + "All", t -> true);
    }

    static <T> MenuItemFilter<T> of(String name, Predicate<T> predicate) {
        return new MenuItemFilter<T>() {
            @Override
            public String getName() {
                return ChatColor.GOLD + name;
            }

            @Override
            public boolean test(T t) {
                return predicate.test(t);
            }
        };
    }

}
