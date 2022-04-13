package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankTransferEvent extends SingleBankEvent implements Cancellable {

	private final OfflinePlayer newOwner;
	private boolean cancelled;

	public BankTransferEvent(CommandSender sender, Bank bank, OfflinePlayer newOwner) {
		super(sender, bank);
		this.newOwner = newOwner;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public OfflinePlayer getNewOwner() {
		return newOwner;
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
