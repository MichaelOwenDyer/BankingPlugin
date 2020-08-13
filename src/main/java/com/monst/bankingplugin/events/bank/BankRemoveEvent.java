package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class BankRemoveEvent extends SingleBankEvent implements Cancellable {

	private boolean cancelled;

	public BankRemoveEvent(CommandSender sender, Bank bank) {
		super(sender, bank);
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
