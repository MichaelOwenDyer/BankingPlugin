package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import java.util.HashSet;
import java.util.Set;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Set<Bank> banks;


	public InterestEvent(BankingPlugin plugin) {
		this(plugin, Bukkit.getConsoleSender());
	}

	public InterestEvent(BankingPlugin plugin, CommandSender sender) {
		this(plugin, sender, plugin.getBankRepository().getAll());
	}

	public InterestEvent(BankingPlugin plugin, Set<Bank> banks) {
		this(plugin, Bukkit.getConsoleSender(), banks);
	}

	public InterestEvent(BankingPlugin plugin, CommandSender sender, Set<Bank> banks) {
		super(plugin, sender);
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
