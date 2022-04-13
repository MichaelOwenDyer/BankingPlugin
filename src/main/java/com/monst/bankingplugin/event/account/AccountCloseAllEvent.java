package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.command.account.AccountCloseAll;
import com.monst.bankingplugin.entity.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collection;

/**
 * This event is fired when multiple accounts are removed at once and the
 * {@link AccountCloseAll} command.
 */
public class AccountCloseAllEvent extends MultiAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountCloseAllEvent(CommandSender sender, Collection<Account> accounts) {
		super(sender, accounts);
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
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
