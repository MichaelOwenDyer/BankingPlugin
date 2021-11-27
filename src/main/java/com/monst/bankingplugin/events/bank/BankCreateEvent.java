package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.PlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankCreateEvent extends SingleBankEvent implements Cancellable, PlayerEvent {

	private boolean cancelled;

	public BankCreateEvent(Player player, Bank bank) {
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
