package com.monst.bankingplugin.events.bank;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Bank;

public class BankEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CommandSender sender;
	private final Bank bank;
	
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
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
