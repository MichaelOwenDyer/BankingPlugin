package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountPreInfoEvent extends AccountEvent implements Cancellable {

	private boolean cancelled;
	private boolean verbose;

	public AccountPreInfoEvent(Player p, boolean verbose) {
		super(p, null);
		this.verbose = verbose;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public boolean isVerbose() {
		return verbose;
	}

}
