package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Observable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents either a {@link Bank} or an {@link Account}.
 */
public abstract class BankingEntity extends Observable implements Ownable, Nameable, Persistable<Integer> {

	protected int id;
	protected String name;
	protected OfflinePlayer owner;
	protected Set<OfflinePlayer> coowners;

	public OfflinePlayer getOwner() {
		return owner;
	}

	public Set<OfflinePlayer> getCoOwners() {
		coowners.removeIf(Objects::isNull);
		return new HashSet<>(coowners);
	}

	public void trustPlayer(OfflinePlayer p) {
		if (p != null)
			coowners.add(p);
		notifyObservers();
		plugin.getDatabase().addCoowner(this, p, null);
	}

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
		if (this.id == -1)
			this.id = id;
	}

	/**
	 * Compiles a nicely formatted text-wall of information about this ownable.
	 * @return a {@link String} with lots of useful information
	 */
	public abstract String toConsolePrintout();

}
