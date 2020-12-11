package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents either a {@link Bank} or an {@link Account}.
 */
public abstract class Ownable extends Observable implements Nameable {

	protected int id;
	protected String name;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners;

	/**
	 * @param p the player in question
	 * @return whether or not the player is the owner of this ownable.
	 */
	public boolean isOwner(OfflinePlayer p) {
		if (owner == null || p == null)
			return false;
		return Utils.samePlayer(owner, p);
	}

	/**
	 * @param p the player in question
	 * @return whether or not the player is a co-owner of this ownable
	 */
	public boolean isCoowner(OfflinePlayer p) {
		return p != null && coowners.contains(p);
	}

	/**
	 * @param p the player in question
	 * @return whether or not the player is either a co-owner or the owner of this ownable.
	 */
	public boolean isTrusted(OfflinePlayer p) {
		return p != null && (isOwner(p) || isCoowner(p));
	}

	/**
	 * @return the owner of this ownable.
	 */
	public OfflinePlayer getOwner() {
		return owner;
	}

	/**
	 * Gets the players trusted on this ownable, excluding the owner.
	 * @return a {@link Set<OfflinePlayer>} containing the current co-owners.
	 */
	public Set<OfflinePlayer> getCoowners() {
		coowners.removeIf(Objects::isNull);
		return new HashSet<>(coowners);
	}

	/**
	 * Gets all players trusted on this ownable, including the owner.
	 * @return a {@link Set<OfflinePlayer>} containing all players trusted on this ownable.
	 */
	public Set<OfflinePlayer> getTrustedPlayers() {
		if (owner == null)
			return getCoowners();
		return Utils.mergeCollections(Collections.singleton(getOwner()), getCoowners());
	}

	/**
	 * @return whether or not any player trusted on this ownable is online.
	 */
	public boolean isTrustedPlayerOnline() {
		return getTrustedPlayers().stream().anyMatch(OfflinePlayer::isOnline);
	}

	/**
	 * Adds a player to the co-owners of this ownable.
	 * @param p the player to be added
	 */
	public void trustPlayer(OfflinePlayer p) {
		if (p != null)
			coowners.add(p);
		notifyObservers();
		plugin.getDatabase().addCoowner(this, p, null);
	}

	/**
	 * Removes a player from co-ownership of this ownable.
	 * @param p the player to be removed
	 */
	public void untrustPlayer(OfflinePlayer p) {
		if (p != null)
			coowners.remove(p);
		notifyObservers();
		plugin.getDatabase().removeCoowner(this, p, null);
	}

	/**
	 * Gets the (possibly colorized) name of the owner of this ownable.
	 * @return a {@link String} with the owner's name, if it can be found.
	 */
	public String getOwnerDisplayName() {
		if (owner == null)
			return ChatColor.RED + "ADMIN";
		if (!owner.hasPlayedBefore())
			return ChatColor.DARK_GRAY + owner.getUniqueId().toString();
		return owner.isOnline() ? owner.getPlayer().getDisplayName() : owner.getName();
	}

	/**
	 * Transfers ownership of this ownable to another {@link OfflinePlayer}
	 * @param newOwner The {@link OfflinePlayer} who this ownable should be transferred to
	 */
	public abstract void transferOwnership(OfflinePlayer newOwner);

	/**
	 * Compiles a nicely formatted text-wall of information about this ownable.
	 * @return a {@link String} with lots of useful information
	 */
	public abstract String getInformation();

	/**
	 * Gets the ID of this ownable, if it exists.
	 * An ownable will not have an ID until it has been added to the {@link com.monst.bankingplugin.sql.Database}
	 * @return Whether the ownable has an ID or not
	 */
	public boolean hasID() {
		return id != -1;
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		if (this.id == -1)
			this.id = id;
	}

	@Override
	public String getRawName() {
		return name;
	}

}
