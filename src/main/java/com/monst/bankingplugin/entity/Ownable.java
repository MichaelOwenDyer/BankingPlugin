package com.monst.bankingplugin.entity;

import org.bukkit.OfflinePlayer;

import java.util.Set;

public interface Ownable {

    /**
     * @return the owner of this ownable.
     */
    OfflinePlayer getOwner();

    /**
     * Transfers ownership of this ownable to another {@link OfflinePlayer}
     * @param player The {@link OfflinePlayer} who this ownable should be transferred to
     */
    void setOwner(OfflinePlayer player);

    /**
     * @param player the player in question
     * @return whether or not the player is the owner of this ownable.
     */
    default boolean isOwner(OfflinePlayer player) {
        if (getOwner() == null || player == null)
            return false;
        return getOwner().getUniqueId().equals(player.getUniqueId());
    }

    /**
     * Gets the players trusted on this ownable, excluding the owner.
     * @return a {@link Set<OfflinePlayer>} containing the current co-owners.
     */
    Set<OfflinePlayer> getCoOwners();

    /**
     * @return whether the ownable has any co-owners
     */
    default boolean hasCoOwners() {
        return !getCoOwners().isEmpty();
    }

    /**
     * @param player the player in question
     * @return whether the player is a co-owner of this ownable
     */
    default boolean isCoOwner(OfflinePlayer player) {
        return player != null && getCoOwners().contains(player);
    }

    /**
     * @param player the player in question
     * @return whether or not the player is either a co-owner or the owner of this ownable.
     */
    default boolean isTrusted(OfflinePlayer player) {
        return isOwner(player) || isCoOwner(player);
    }

    /**
     * Adds a player to the co-owners of this ownable.
     * @param player the player to be added
     */
    void trustPlayer(OfflinePlayer player);

    /**
     * Removes a player from co-ownership of this ownable.
     * @param player the player to be removed
     */
    void untrustPlayer(OfflinePlayer player);

    /**
     * Gets all players trusted on this ownable, including the owner.
     * @return a {@link Set<OfflinePlayer>} containing all players trusted on this ownable.
     */
    default Set<OfflinePlayer> getTrustedPlayers() {
        Set<OfflinePlayer> trustedPlayers = getCoOwners();
        if (getOwner() != null)
            trustedPlayers.add(getOwner());
        return trustedPlayers;
    }

}
