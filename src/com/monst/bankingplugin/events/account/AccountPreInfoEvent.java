package com.monst.bankingplugin.events.account;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountPreInfoEvent extends Event implements Cancellable {

	private boolean cancelled;
	private CommandSender sender;
	private static HandlerList handlers = new HandlerList();

	public AccountPreInfoEvent(CommandSender sender) {
		this.sender = sender;
	}

	public CommandSender getSender() {
		return sender;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
