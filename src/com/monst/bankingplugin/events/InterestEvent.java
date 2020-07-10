package com.monst.bankingplugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.BankingPlugin;

import net.milkbowl.vault.economy.Economy;

public class InterestEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private BankingPlugin plugin;
	private Economy econ;

	public InterestEvent(BankingPlugin plugin) {
		this.plugin = plugin;
		this.econ = plugin.getEconomy();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public BankingPlugin getPlugin() {
		return plugin;
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
}