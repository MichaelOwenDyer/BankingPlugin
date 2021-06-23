package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when an account is removed by a player.
 */
public class AccountRemoveEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountRemoveEvent(@Nonnull Player player, @Nonnull Account account) {
		super(player, account);
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
