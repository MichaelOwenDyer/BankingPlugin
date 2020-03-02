package com.monst.bankingplugin.events.account;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Account;

public class AccountRemoveAllEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
	private boolean cancelled;
	private CommandSender sender;
	private List<Account> affectedAccounts;

	public AccountRemoveAllEvent(CommandSender sender, List<Account> affectedAccounts) {
		this.sender = sender;
		this.affectedAccounts = affectedAccounts;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;		
	}

	public List<Account> getAffectedAccounts() {
		return affectedAccounts;
	}

	public CommandSender getSender() {
		return sender;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
