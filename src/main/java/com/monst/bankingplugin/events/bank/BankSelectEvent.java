package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BankSelectEvent extends SingleBankEvent implements Cancellable {

	private boolean cancelled;

	public BankSelectEvent(Player player, Bank bank) {
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
