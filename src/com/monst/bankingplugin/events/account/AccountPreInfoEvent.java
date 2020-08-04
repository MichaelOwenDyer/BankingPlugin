package com.monst.bankingplugin.events.account;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountPreInfoEvent extends AccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountPreInfoEvent(Player player) {
		super(player, null);
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
