package com.monst.bankingplugin.events.bank;

import java.util.Collection;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Bank;

public class BankRemoveAllEvent extends MultiBankEvent implements Cancellable {

	private boolean cancelled;

	public BankRemoveAllEvent(CommandSender sender, Collection<Bank> banks) {
		super(sender, banks);
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
