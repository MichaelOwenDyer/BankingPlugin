package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.banking.BankingEntityField;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A repository of {@link BankingEntity}s, e.g. Banks or Accounts.
 * @param <Entity> the {@link BankingEntity} of this repository
 * @param <Field> the corresponding {@link BankingEntityField}
 */
public interface Repository<Entity extends BankingEntity, Field extends BankingEntityField<Entity>> {

    /**
     * Gets all accounts or banks on the server.
     *
     * @return a {@link Set} of all entities.
     */
    Set<Entity> getAll();

    /**
     * Gets the entity located at a certain {@link AccountLocation}.
     * @return a single entity or null.
     */
    Entity getAt(AccountLocation location);

    /**
     * Gets the entity located at a certain {@link Block}.
     * @return a single entity or null.
     */
    Entity getAt(Block block);

    /**
     * Gets all entities on the server that fulfill a certain {@link Predicate}.
     *
     * @return A {@link Set} containing all entities fulfilling the predicate.
     */
    default Set<Entity> getMatching(Predicate<? super Entity> filter) {
        return Utils.filter(getAll(), filter);
    }

    /**
     * Gets the entity with the specified ID.
     * @return a single entity or null.
     */
    default Entity getByID(int id) {
        return getAll().stream().filter(entity -> entity.getID() == id).findFirst().orElse(null);
    }

    /**
     * Gets the entity described by the specified string, either by name or by ID.
     * @param identifier a string describing the requested entity.
     * @return a single entity or null.
     */
    default Entity getByIdentifier(String identifier) {
        try {
            return getByID(Integer.parseInt(identifier));
        } catch (NumberFormatException e) {
            return getByName(identifier);
        }
    }

    /**
     * Gets the entity with the specified name.
     * @return a single entity or null.
     */
    default Entity getByName(String name) {
        return getAll().stream().filter(b -> b.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Gets all entities owned by the specified player.
     * @return A {@link Set} of all entities owned by the player.
     */
    default Set<Entity> getOwnedBy(OfflinePlayer owner) {
        return getMatching(entity -> entity.isOwner(owner));
    }

    /**
     * Adds the specified entity to this repository.
     * @param addToDatabase whether to also add the entity to the database.
     */
    default void add(Entity entity, boolean addToDatabase) {
        add(entity, addToDatabase, Callback.doNothing());
    }

    /**
     * Adds the specified entity to this repository, and returns the entity's ID to the {@link Callback}.
     * If the entity is being added to the database (and does not have an ID yet), the returned ID will be newly generated.
     * @param addToDatabase whether to also add the entity to the database.
     * @param callback the callback the entity ID will be returned to.
     */
    void add(Entity entity, boolean addToDatabase, Callback<Integer> callback);

    /**
     * Updates the specified fields of the specified entity in the database.
     * @param entity the entity to update in the database
     * @param fields the fields to update in the database
     */
    default void update(Entity entity, Field... fields) {
        update(entity, Callback.doNothing(), fields);
    }

    /**
     * Updates the specified fields of the specified entity in the database, and returns {@code null} to the {@link Callback}.
     * @param entity the entity to update in the database
     * @param callback the callback that will be called after updating is completed.
     * @param fields the fields to update in the database
     */
    void update(Entity entity, Callback<Void> callback, Field... fields);

    /**
     * Removes the specified entity from this repository.
     * @param removeFromDatabase Whether to also remove the entity from the database.
     */
    default void remove(Entity entity, boolean removeFromDatabase) {
        remove(entity, removeFromDatabase, Callback.doNothing());
    }

    /**
     * Removes the specified entity from this repository, and returns {@code null} to the {@link Callback}.
     * @param removeFromDatabase Whether to also remove the entity from the database.
     * @param callback the callback that will be called after removal is completed
     */
    void remove(Entity entity, boolean removeFromDatabase, Callback<Void> callback);

}
