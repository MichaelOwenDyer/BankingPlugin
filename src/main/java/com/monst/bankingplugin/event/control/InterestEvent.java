package com.monst.bankingplugin.event.control;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The event at the heart of BankingPlugin.
 * This event is called at the interest payout times of all banks on the server.
 */
public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Set<Bank> banks;

	public InterestEvent(CommandSender sender, Collection<Bank> banks) {
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
