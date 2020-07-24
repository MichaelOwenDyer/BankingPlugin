package com.monst.bankingplugin.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Ownable {

	protected int id;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners = new HashSet<>();

	public boolean isOwner(OfflinePlayer player) {
		if (owner == null || player == null)
			return false;
		return owner.getUniqueId().equals(player.getUniqueId());
	}

	public String getOwnerDisplayName() {
		if (owner == null)
			return ChatColor.RED + "ADMIN";
		if (!owner.hasPlayedBefore())
			return ChatColor.DARK_GRAY + owner.getUniqueId().toString();
		return owner.isOnline() ? owner.getPlayer().getDisplayName() : owner.getName();
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public boolean isTrustedPlayerOnline() {
		if (owner == null)
			return false;
		return owner.isOnline() || (coowners != null && coowners.stream().anyMatch(OfflinePlayer::isOnline));
	}

	public boolean isTrusted(OfflinePlayer p) {
		return p != null && (isOwner(p) || isCoowner(p));
	}

	public boolean isCoowner(OfflinePlayer p) {
		return (p != null && coowners != null) && coowners.contains(p);
	}

	public void trustPlayer(OfflinePlayer p) {
		if (p == null || coowners == null)
			return;
		coowners.add(p);
	}

	public void untrustPlayer(OfflinePlayer p) {
		if (p == null || coowners == null)
			return;
		coowners.remove(p);
	}

	public Set<OfflinePlayer> getTrustedPlayers() {
		Set<OfflinePlayer> trustedPlayers = new HashSet<>();
		if (owner != null)
			trustedPlayers.add(owner);
		if (coowners != null)
			trustedPlayers.addAll(coowners);
		return trustedPlayers;
	}

	public Set<OfflinePlayer> getCoowners() {
		if (coowners == null)
			return new HashSet<>();
		return Collections.unmodifiableSet(coowners);
	}

	public abstract void transferOwnership(OfflinePlayer newOwner);

	public abstract TextComponent getInfoButton(CommandSender sender);

	public abstract TextComponent getInformation(CommandSender sender);

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
