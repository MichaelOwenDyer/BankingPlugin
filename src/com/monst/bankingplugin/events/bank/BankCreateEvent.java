package com.monst.bankingplugin.events.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Bank;

public class BankCreateEvent extends BankEvent implements Cancellable {
	
	private boolean cancelled;

	public BankCreateEvent(Player player, Bank bank) {
		super(player, bank);
	}

	public Player getPlayer() {
		return (Player) super.getSender();
	}
	
	@Override
	public Bank getBank() {
		return super.getBank();
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
