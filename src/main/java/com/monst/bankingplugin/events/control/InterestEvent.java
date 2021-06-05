package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.banking.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import java.util.HashSet;
import java.util.Set;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Set<Bank> banks;


	public InterestEvent() {
		this(Bukkit.getConsoleSender(), PLUGIN.getBankRepository().getAll());
	}

	public InterestEvent(Set<Bank> banks) {
		this(Bukkit.getConsoleSender(), banks);
	}

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
