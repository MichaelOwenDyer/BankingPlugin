package com.monst.bankingplugin.events.interest;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountUtils;

import net.milkbowl.vault.economy.Economy;

public class InterestEvent extends Event {
	
	private HandlerList handlers = new HandlerList();
	private BankingPlugin plugin;
	private AccountUtils accountUtils;
	private Economy econ;

	public InterestEvent(BankingPlugin plugin) {
		this.plugin = plugin;
		this.accountUtils = plugin.getAccountUtils();
		this.econ = plugin.getEconomy();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public BankingPlugin getInstance() {
		return plugin;
	}
	
	public AccountUtils getAccountUtils() {
		return accountUtils;
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
}
