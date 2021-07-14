package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import java.util.HashSet;
import java.util.Set;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Set<Bank> banks;

	public InterestEvent(CommandSender sender, Set<Bank> banks) {
		super(sender);
		this.banks = new HashSet<>(banks);
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
