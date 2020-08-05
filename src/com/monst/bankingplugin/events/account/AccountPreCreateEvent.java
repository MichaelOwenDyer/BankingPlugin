package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountPreCreateEvent extends SingleAccountEvent implements Cancellable {
	
	private boolean cancelled;
	private final String[] args;

	public AccountPreCreateEvent(Player player, String[] args) {
		super(player, null);
		this.args = args;
	}

	public String[] getArgs() {
		return args;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

}
