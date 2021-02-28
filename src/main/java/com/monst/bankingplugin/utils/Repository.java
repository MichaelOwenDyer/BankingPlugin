package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.banking.BankingEntity;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface Repository<O extends BankingEntity> {

    Set<O> getAll();

    O getAt(Location location);

    /**
     * Gets all banks on the server that fulfill a certain {@link Predicate}
     *
     * @return A new {@link HashSet} containing all banks
     */
    default Set<O> getMatching(Predicate<? super O> filter) {
        return Utils.filter(getAll(), filter);
    }

    default O getByID(int id) {
        return getAll().stream().filter(o -> o.getID() == id).findFirst().orElse(null);
    }

    default O getByIdentifier(String identifier) {
        try {
            return getByID(Integer.parseInt(identifier));
        } catch (NumberFormatException e) {
            return getByName(identifier);
        }
    }

    default O getByName(String name) {
        return getAll().stream().filter(b -> b.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Get the number of ownables owned by a certain player
     *
     * @param owner Player whose ownables should be returned
     * @return The number of ownables owned by the player
     */
    default Set<O> getOwnedBy(OfflinePlayer owner) {
        return getMatching(o -> o.isOwner(owner));
    }

    default void add(O o, boolean addToDatabase) {
        add(o, addToDatabase, null);
    }

    void add(O o, boolean addToDatabase, Callback<Integer> callback);

    default void remove(O o, boolean removeFromDatabase) {
        remove(o, removeFromDatabase, null);
    }

    void remove(O o, boolean removeFromDatabase, Callback<Void> callback);

    int getLimit(Player player);

}
