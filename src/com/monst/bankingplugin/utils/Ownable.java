package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents either a {@link Bank} or an {@link Account}.
 */
public abstract class Ownable {

	protected int id;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners = new HashSet<>();

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
		return (p != null && coowners != null) && coowners.contains(p);
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
	 * Get the players trusted on this ownable, excluding the owner.
	 * @return a {@link Set<OfflinePlayer>} containing the current co-owners.
	 */
	public Set<OfflinePlayer> getCoowners() {
		if (coowners == null)
			return new HashSet<>();
		return Collections.unmodifiableSet(coowners);
	}

	/**
	 * Get all players trusted on this ownable, including the owner.
	 * @return a {@link Set<OfflinePlayer>} containing all players trusted on this ownable.
	 */
	public Set<OfflinePlayer> getTrustedPlayers() {
		Set<OfflinePlayer> trustedPlayers = new HashSet<>();
		if (owner != null)
			trustedPlayers.add(owner);
		if (coowners != null)
			trustedPlayers.addAll(coowners);
		return trustedPlayers;
	}

	/**
	 * @return whether or not any player trusted on this ownable is online.
	 */
	public boolean isTrustedPlayerOnline() {
		return getTrustedPlayers().stream().anyMatch(OfflinePlayer::isOnline);
	}

	/**
	 * Add a player to the co-owners of this ownable.
	 * @param p the player to be added
	 */
	public void trustPlayer(OfflinePlayer p) {
		if (p == null || coowners == null)
			return;
		coowners.add(p);
	}

	/**
	 * Remove a player from co-ownership of this ownable.
	 * @param p the player to be removed
	 */
	public void untrustPlayer(OfflinePlayer p) {
		if (p == null || coowners == null)
			return;
		coowners.remove(p);
	}

	/**
	 * Get the (possibly colorized) name of the owner of this ownable.
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
	 * Creates a clickable chat message that will open the GUI screen for this ownable.
	 * @param sender The {@link CommandSender} who requested the button
	 * @return a {@link TextComponent} with a clickable chat message
	 */
	public abstract TextComponent getInfoButton(CommandSender sender);

	/**
	 * Compiles a nicely formatted text-wall of information about this ownable.
	 * @param sender The {@link CommandSender} who requested information
	 * @return a {@link TextComponent} with lots of useful information
	 */
	public abstract TextComponent getInformation(CommandSender sender);

	/**
	 * An ownable might will not have an ID if it has not been added to the {@link com.monst.bankingplugin.sql.Database}
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

}
