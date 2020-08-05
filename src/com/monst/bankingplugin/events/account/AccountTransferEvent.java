package com.monst.bankingplugin.events.account;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

public class AccountTransferEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
	private final OfflinePlayer newOwner;

	public AccountTransferEvent(Player player, Account account, OfflinePlayer newOwner) {
		super(player, account);
		this.newOwner = newOwner;
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
