package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.PlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankSelectEvent extends SingleBankEvent implements Cancellable, PlayerEvent {

	private boolean cancelled;

	public BankSelectEvent(Player player, Bank bank) {
		super(player, bank);
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
