package com.monst.bankingplugin.events.account;

import java.util.Collection;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Account;

public class AccountRemoveAllEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
	private boolean cancelled;
	private final Collection<Account> affectedAccounts;

	public AccountRemoveAllEvent(Collection<Account> accounts) {
		this.affectedAccounts = accounts;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;		
	}

	public Collection<Account> getAffectedAccounts() {
		return affectedAccounts;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
