package com.monst.bankingplugin.events.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.selections.Selection;

public class BankResizeEvent extends BankEvent implements Cancellable {
	
	private boolean cancelled;
	private Selection newSelection;

	public BankResizeEvent(Player player, Bank bank, Selection newSelection) {
		super(player, bank);
		this.newSelection = newSelection;
	}
	
	@Override
	public Player getPlayer() {
		return super.getPlayer();
	}
	
	@Override
	public Bank getBank() {
		return super.getBank();
	}

	public Selection getOldSelection() {
		return super.getBank().getSelection();
	}

	public Selection getNewSelection() {
		return newSelection;
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
