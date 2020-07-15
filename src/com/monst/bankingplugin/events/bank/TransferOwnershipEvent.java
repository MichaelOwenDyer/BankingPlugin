package com.monst.bankingplugin.events.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Bank;

public class TransferOwnershipEvent extends BankEvent implements Cancellable {

	private OfflinePlayer newOwner;
	private boolean cancelled;

	public TransferOwnershipEvent(Player p, Bank bank, OfflinePlayer newOwner) {
		super(p, bank);
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
