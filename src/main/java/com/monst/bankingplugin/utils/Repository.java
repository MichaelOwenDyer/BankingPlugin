package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.banking.Ownable;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface Repository<O extends Ownable> {

    Set<O> get();

    O get(Location location);

    /**
     * Gets all banks on the server that fulfill a certain {@link Predicate}
     *
     * @return A new {@link HashSet} containing all banks
     */
    default Set<O> get(Predicate<? super O> filter) {
        return Utils.filter(get(), filter);
    }

    default O get(int id) {
        return get().stream().filter(o -> o.getID() == id).findFirst().orElse(null);
    }

    /**
     * Get the number of ownables owned by a certain player
     *
     * @param owner Player whose ownables should be returned
     * @return The number of ownables owned by the player
     */
    default Set<O> getOwnedBy(OfflinePlayer owner) {
        return get(o -> o.isOwner(owner));
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
