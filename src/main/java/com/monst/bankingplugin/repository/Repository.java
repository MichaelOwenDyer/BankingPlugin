package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.exceptions.NotFoundException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public interface Repository<Entity extends BankingEntity> {

    Set<Entity> getAll();

    Entity getAt(ChestLocation location) throws NotFoundException;

    /**
     * Gets all banks on the server that fulfill a certain {@link Predicate}
     *
     * @return A new {@link HashSet} containing all banks
     */
    default Set<Entity> getMatching(Predicate<? super Entity> filter) {
        return Utils.filter(getAll(), filter);
    }

    default Entity getByID(int id) {
        return getAll().stream().filter(entity -> entity.getID() == id).findFirst().orElse(null);
    }

    default Entity getByIdentifier(String identifier) {
        try {
            return getByID(Integer.parseInt(identifier));
        } catch (NumberFormatException e) {
            return getByName(identifier);
        }
    }

    default Entity getByName(String name) {
        return getAll().stream().filter(b -> b.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Get the number of ownables owned by a certain player
     *
     * @param owner Player whose ownables should be returned
     * @return The number of ownables owned by the player
     */
    default Set<Entity> getOwnedBy(OfflinePlayer owner) {
        return getMatching(entity -> entity.isOwner(owner));
    }

    default void add(Entity entity, boolean addToDatabase) {
        add(entity, addToDatabase, Callback.blank());
    }

    void add(Entity entity, boolean addToDatabase, Callback<Integer> callback);

    default void remove(Entity entity, boolean removeFromDatabase) {
        remove(entity, removeFromDatabase, Callback.blank());
    }

    void remove(Entity entity, boolean removeFromDatabase, Callback<Void> callback);

}