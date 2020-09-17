package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a {@link Player} with a {@link com.monst.bankingplugin.utils.ClickType.CreateClickType}
 * clicks on a chest and creates an account.
 */
public class AccountCreateEvent extends SingleAccountEvent implements Cancellable {
	
	private boolean cancelled;

	public AccountCreateEvent(@Nonnull Player player, @Nonnull Account account) {
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
