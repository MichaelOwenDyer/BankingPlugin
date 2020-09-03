package com.monst.bankingplugin.utils;

import org.bukkit.entity.Player;

public interface Confirmable<T> {

    default boolean isConfirmed(Player p, T t) {
        if (hasEntry(p, t)) {
            removeEntry(p, t);
            return true;
        }
        putEntry(p, t);
        return false;
    }

    boolean hasEntry(Player p, T t);

    void putEntry(Player p, T t);

    void removeEntry(Player p, T t);

}
