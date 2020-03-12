package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.md_5.bungee.api.ChatColor;

public class Bank {
	
	private final BankingPlugin plugin;
	private boolean created;
	
	private int id;
	private final String name;
	private final World world;
	private Selection selection;

	private Set<Account> accounts = null;

	public Bank(BankingPlugin plugin, String name, Selection selection) {
		this(-1, plugin, name, selection);
	}
	
	public Bank(int id, BankingPlugin plugin, String name, Selection selection) {
		this.id = id;
		this.plugin = plugin;
		this.name = name;
		this.world = selection.getWorld();
		this.selection = selection;
	}
	
	public boolean create(boolean showConsoleMessages) {
		if (created)
			return false;
		
		plugin.debug("Creating bank (#" + id + ")");

		accounts = new HashSet<>();

		created = true;
		return true;
	}
	
	public String getName() {
		return name;
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
	
	public World getWorld() {
		return world;
	}

	public Selection getSelection() {
		return selection;
	}

	public List<Location> getVertices() {
		return plugin.getBankUtils().getVertices(selection);
	}

	public String getSelectionType() {
		if (selection instanceof CuboidSelection)
			return "CUBOID";
		if (selection instanceof Polygonal2DSelection)
			return "POLYGONAL";
		return "";
	}

	public Collection<Account> getAccounts() {
		return accounts;
	}
	
	public void addAccount(Account account) {
		if (created)
			accounts.add(account);
	}
	
	public void addAccount(Collection<Account> newAccounts) {
		if (created)
			newAccounts.forEach(account -> accounts.add(account));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Bank otherBank = (Bank) o;
		return id != -1 && id == otherBank.id;
	}

	@Override
	public int hashCode() {
		return id != -1 ? id : super.hashCode();
	}

	public BigDecimal getTotalValue() {
		if (created)
			return accounts.stream().map(account -> account.getStatus().getBalance()).reduce(BigDecimal.ZERO,
					(value, sum) -> sum.add(value));
		return BigDecimal.ZERO;
	}
	
	public IntStream getAccountIDs() {
		return accounts.stream().mapToInt(account -> account.getID());
	}
	
	public String getInfoAsString() {
		return ChatColor.GOLD + getName()
		+ ChatColor.GREEN + " with "
		+ ChatColor.AQUA + accounts.size()
		+ ChatColor.GREEN + " accounts.";
	}

}
