package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * This event is fired when a player uses the {@link com.monst.bankingplugin.commands.account.AccountInfo}
 * command without specifying an account ID.
 */
public class AccountInfoCommandEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountInfoCommandEvent(Player player) {
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
