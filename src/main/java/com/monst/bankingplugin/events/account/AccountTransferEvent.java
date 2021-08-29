package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when the owner of an account changes.
 */
public class AccountTransferEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
	private final OfflinePlayer newOwner;

	public AccountTransferEvent(Player player, Account account, OfflinePlayer newOwner) {
		super(player, account);
		this.newOwner = newOwner;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public OfflinePlayer getNewOwner() {
		return newOwner;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

}
