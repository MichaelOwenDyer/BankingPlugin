package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import java.util.Collection;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;
	private final Collection<Bank> banks;


	public InterestEvent(BankingPlugin plugin) {
		this(plugin, Bukkit.getConsoleSender());
	}

	public InterestEvent(BankingPlugin plugin, CommandSender sender) {
		this(plugin, sender, plugin.getBankUtils().getBanksCopy());
	}

	public InterestEvent(BankingPlugin plugin, Collection<Bank> banks) {
		this(plugin, Bukkit.getConsoleSender(), banks);
	}

	public InterestEvent(BankingPlugin plugin, CommandSender sender, Collection<Bank> banks) {
		super(plugin, sender);
		this.banks = banks;
	}

	public Collection<Bank> getBanks() {
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
