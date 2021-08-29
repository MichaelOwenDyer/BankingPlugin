package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.geo.regions.BankRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BankResizeEvent extends SingleBankEvent implements Cancellable {

	private boolean cancelled;
	private final BankRegion newBankRegion;

	public BankResizeEvent(Player player, Bank bank, BankRegion newBankRegion) {
		super(player, bank);
		this.newBankRegion = newBankRegion;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public BankRegion getPreviousRegion() {
		return getBank().getRegion();
	}

	public BankRegion getNewRegion() {
		return newBankRegion;
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
