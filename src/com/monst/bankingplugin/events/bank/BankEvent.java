package com.monst.bankingplugin.events.bank;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Bank;

public class BankEvent extends Event {

	private static HandlerList handlers = new HandlerList();
	private CommandSender sender;
	private Bank bank;
	
	public BankEvent(CommandSender sender, Bank bank) {
		this.sender = sender;
		this.bank = bank;
	}
	
	public CommandSender getSender() {
		return sender;
	}
	
	public Bank getBank() {
		return bank;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
