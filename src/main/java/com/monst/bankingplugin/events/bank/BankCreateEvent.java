package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BankCreateEvent extends SingleBankEvent implements Cancellable {

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
