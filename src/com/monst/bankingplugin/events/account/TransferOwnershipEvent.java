package com.monst.bankingplugin.events.account;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

public class TransferOwnershipEvent extends AccountEvent implements Cancellable {

	private final OfflinePlayer newOwner;
	private boolean cancelled;

	public TransferOwnershipEvent(Player p, Account account, OfflinePlayer newOwner) {
		super(p, account);
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
