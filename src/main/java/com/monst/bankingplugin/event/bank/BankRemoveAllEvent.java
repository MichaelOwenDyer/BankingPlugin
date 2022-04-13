package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class BankRemoveAllEvent extends MultiBankEvent implements Cancellable {

	private boolean cancelled;

	public BankRemoveAllEvent(CommandSender sender, Collection<Bank> banks) {
		super(sender, banks);
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
