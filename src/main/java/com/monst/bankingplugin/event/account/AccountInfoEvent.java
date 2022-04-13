package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.gui.AccountGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when info about an {@link Account} is fetched.
 * This can be either a player opening an {@link AccountGUI} or the console printing out
 * an info dump and the command "account info [id]".
 */
public class AccountInfoEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountInfoEvent(Player player, Account account) {
		super(player, account);
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
