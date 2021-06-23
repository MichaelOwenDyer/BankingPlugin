package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.geo.regions.BankRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BankResizeEvent extends SingleBankEvent implements Cancellable {

	private boolean cancelled;
	private final BankRegion newBankRegion;

	public BankResizeEvent(Player player, Bank bank, BankRegion newBankRegion) {
		super(player, bank);
		this.newBankRegion = newBankRegion;
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
