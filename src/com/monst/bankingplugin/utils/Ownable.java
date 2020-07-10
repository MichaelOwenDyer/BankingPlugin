package com.monst.bankingplugin.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.OfflinePlayer;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.Bank.BankType;

import net.md_5.bungee.api.ChatColor;

public abstract class Ownable {

	protected int id;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners = new HashSet<>();

	public boolean isOwner(OfflinePlayer player) {
		if (owner == null)
			return false;
		return owner.getUniqueId().equals(player.getUniqueId());
	}

	public String getOwnerDisplayName() {
		if (owner == null)
			return "";
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
		return owner.isOnline() || (coowners != null ? coowners.stream().anyMatch(p -> p.isOnline()) : false);
	}

	public boolean isTrusted(OfflinePlayer p) {
		return p != null ? isOwner(p) || isCoowner(p) : false;
	}

	public boolean isCoowner(OfflinePlayer p) {
		return p != null && coowners != null ? coowners.contains(p) : false;
	}

	public void trustPlayer(OfflinePlayer p) {
		if (coowners != null)
			coowners.add(p);
	}

	public void untrustPlayer(OfflinePlayer p) {
		if (coowners != null)
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

	public void transferOwnership(OfflinePlayer newOwner, boolean trustPrevOwner) {
		if (this instanceof Account && newOwner == null)
			return;
		OfflinePlayer prevOwner = owner;
		owner = newOwner;
		if (this instanceof Bank)
			((Bank) this).setBankType(owner == null ? BankType.ADMIN : BankType.PLAYER);
		if (trustPrevOwner)
			coowners.add(prevOwner);
	}

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
