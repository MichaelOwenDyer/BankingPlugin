package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * This class represents either a {@link Bank} or an {@link Account}.
 */
public abstract class BankingEntity extends Observable implements Ownable, Nameable, Persistable<Integer> {

	protected static final BankingPlugin plugin = BankingPlugin.getInstance();

	protected int id;
	protected String name;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners;

	protected BankingEntity(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.coowners = coowners;
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public Set<OfflinePlayer> getCoOwners() {
		coowners.removeIf(Objects::isNull);
		return new HashSet<>(coowners);
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
		return Optional.ofNullable(owner).map(OfflinePlayer::getUniqueId).orElse(null);
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
	public Integer getID() {
		return id;
	}

	@Override
	public void setID(Integer id) {
		if (!hasID())
			this.id = id;
	}

	/**
	 * Compiles a nicely formatted text-wall of information about this ownable.
	 * @return a {@link String} with lots of useful information
	 */
	public abstract String toConsolePrintout();

}
