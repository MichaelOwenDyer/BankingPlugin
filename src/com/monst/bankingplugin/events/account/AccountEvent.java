package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Account;

public abstract class AccountEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final Account account;
	
	public AccountEvent(Player player, Account account) {
		this.player = player;
		this.account = account;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
}
