package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
		if (p != null)
			coowners.add(p);
		notifyObservers();
	}

	public void untrustPlayer(OfflinePlayer p) {
		if (p != null)
			coowners.remove(p);
		notifyObservers();
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

	public UUID getOwnerUUID() {
		return Utils.nonNull(owner, OfflinePlayer::getUniqueId, () -> null);
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
