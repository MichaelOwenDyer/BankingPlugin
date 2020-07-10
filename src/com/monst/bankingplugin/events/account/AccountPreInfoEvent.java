package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountPreInfoEvent extends AccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountPreInfoEvent(Player p) {
		super(p, null);
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
