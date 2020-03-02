package com.monst.bankingplugin.events.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Bank;

public class BankEvent extends Event {

	private static HandlerList handlers = new HandlerList();
	private Player player;
	private Bank bank;
	
	public BankEvent(Player player, Bank bank) {
		this.player = player;
		this.bank = bank;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Bank getBank() {
		return bank;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
