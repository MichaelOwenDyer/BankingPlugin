package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.PlayerEvent;
import com.monst.bankingplugin.geo.regions.BankRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankResizeEvent extends SingleBankEvent implements Cancellable, PlayerEvent {

	private boolean cancelled;
	private final BankRegion previousRegion;
	private final BankRegion newRegion;

	public BankResizeEvent(Player player, Bank bank, BankRegion newRegion) {
		super(player, bank);
		this.previousRegion = bank.getRegion();
		this.newRegion = newRegion;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public BankRegion getNewRegion() {
		return newRegion;
	}

	public BankRegion getPreviousRegion() {
		return previousRegion;
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
