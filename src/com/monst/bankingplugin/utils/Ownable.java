package com.monst.bankingplugin.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.OfflinePlayer;

public abstract class Ownable {

	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners = new HashSet<>();

	public boolean isOwner(OfflinePlayer player) {
		return owner.getUniqueId().equals(player.getUniqueId());
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public boolean isTrustedPlayerOnline() {
		return owner.isOnline() || coowners.stream().anyMatch(p -> p.isOnline());
	}

	public boolean isTrusted(OfflinePlayer p) {
		return p != null ? isOwner(p) || isCoowner(p) : false;
	}

	public boolean isCoowner(OfflinePlayer p) {
		return p != null ? coowners.contains(p) : false;
	}

	public void trustPlayer(OfflinePlayer p) {
		coowners.add(p);
	}

	public void untrustPlayer(OfflinePlayer p) {
		coowners.remove(p);
	}

	public Set<OfflinePlayer> getTrustedPlayersCopy() {
		Set<OfflinePlayer> trustedPlayers = new HashSet<>();
		trustedPlayers.add(owner);
		trustedPlayers.addAll(coowners);
		return Collections.unmodifiableSet(trustedPlayers);
	}

	public Set<OfflinePlayer> getCoowners() {
		return Collections.unmodifiableSet(coowners);
	}

}
