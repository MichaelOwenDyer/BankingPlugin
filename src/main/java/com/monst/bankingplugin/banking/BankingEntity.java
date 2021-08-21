package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * This class represents either a {@link Bank} or an {@link Account}.
 */
public abstract class BankingEntity implements Ownable, Nameable, Persistable, Observable {

	final BankingPlugin plugin;
	int id;
	String name;
	OfflinePlayer owner;
	Set<OfflinePlayer> coowners;
	Set<GUI<?>> observers;

	BankingEntity(BankingPlugin plugin, int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners) {
		super();
		this.plugin = plugin;
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.coowners = coowners;
		this.observers = new HashSet<>();
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public Set<OfflinePlayer> getCoOwners() {
		coowners.removeIf(Objects::isNull);
		return new HashSet<>(coowners);
	}

	@Override
	public Set<GUI<?>> getObservers() {
		return observers;
	}

	public void trustPlayer(OfflinePlayer p) {
		if (p == null)
			return;
		coowners.add(p);
		notifyObservers();
	}

	public void untrustPlayer(OfflinePlayer p) {
		if (p == null)
			return;
		coowners.remove(p);
		notifyObservers();
	}

	public String getOwnerName() {
		return owner == null ? "ADMIN" : owner.getName();
	}

	/**
	 * Gets the (possibly colorized) name of the owner of this ownable.
	 * @return a {@link String} with the owner's name, if it can be found.
	 */
	public String getOwnerDisplayName() {
		if (owner == null)
			return ChatColor.RED + "ADMIN";
		return owner.isOnline() ? owner.getPlayer().getDisplayName() : owner.getName();
	}

	public UUID getOwnerUUID() {
		if (owner == null)
			return null;
		return owner.getUniqueId();
	}

	@Override
	public String getRawName() {
		return name;
	}

	@Override
	public boolean hasID() {
		return id != -1;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public void setID(int id) {
		if (!hasID())
			this.id = id;
	}

}
