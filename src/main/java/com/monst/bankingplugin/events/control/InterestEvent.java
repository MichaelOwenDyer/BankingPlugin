package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.HashSet;
import java.util.Set;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Set<Bank> banks;

	public InterestEvent(CommandSender sender, Set<Bank> banks) {
		super(sender);
		this.banks = new HashSet<>(banks);
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Set<Bank> getBanks() {
		return banks;
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
