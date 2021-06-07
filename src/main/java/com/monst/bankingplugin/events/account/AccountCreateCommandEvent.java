package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * This event is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountCreate} command.
 */
public class AccountCreateCommandEvent extends AccountCommandEvent implements Cancellable {

	private boolean cancelled;

	public AccountCreateCommandEvent(Player player, String[] args) {
		super(player, args);
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
