package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class InterestEvent extends ControlEvent implements Cancellable {

	private boolean cancelled;

	public InterestEvent(BankingPlugin plugin, CommandSender sender) {
		super(plugin, sender);
	}
	
	public InterestEvent(BankingPlugin plugin) {
		super(plugin, Bukkit.getConsoleSender());
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
