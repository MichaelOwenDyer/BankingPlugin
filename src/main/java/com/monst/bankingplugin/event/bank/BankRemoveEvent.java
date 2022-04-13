package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankRemoveEvent extends SingleBankEvent implements Cancellable {

	private boolean cancelled;

	public BankRemoveEvent(CommandSender sender, Bank bank) {
		super(sender, bank);
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
