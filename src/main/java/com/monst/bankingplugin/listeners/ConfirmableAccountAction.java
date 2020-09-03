package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Confirmable;
import org.bukkit.entity.Player;

import java.util.*;

public interface ConfirmableAccountAction extends Confirmable<Integer> {

    Map<UUID, Set<Integer>> unconfirmed = new HashMap<>();

    @Override
    default boolean hasEntry(Player p, Integer id) {
        return unconfirmed.containsKey(p.getUniqueId()) && unconfirmed.get(p.getUniqueId()).contains(id);
    }

    @Override
    default void putEntry(Player p, Integer id) {
        Set<Integer> ids = getEntries(p);
        ids.add(id);
        unconfirmed.put(p.getUniqueId(), ids);
    }

    @Override
    default void removeEntry(Player p, Integer id) {
        Set<Integer> ids = getEntries(p);
        ids.remove(id);
        if (ids.isEmpty()) {
            unconfirmed.remove(p.getUniqueId());
            ClickType.removePlayerClickType(p);
        } else
            unconfirmed.put(p.getUniqueId(), ids);
    }

    default Set<Integer> getEntries(Player p) {
        return unconfirmed.containsKey(p.getUniqueId()) ?
                unconfirmed.get(p.getUniqueId()) : new HashSet<>();
    }

}
