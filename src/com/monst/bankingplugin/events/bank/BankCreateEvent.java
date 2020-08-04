package com.monst.bankingplugin.events.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Bank;

public class BankCreateEvent extends BankEvent implements Cancellable {
	
	private boolean cancelled;

	public BankCreateEvent(Player player, Bank bank) {
		super(player, bank);
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
