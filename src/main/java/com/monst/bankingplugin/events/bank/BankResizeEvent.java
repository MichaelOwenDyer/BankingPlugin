package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.selections.Selection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BankResizeEvent extends SingleBankEvent implements Cancellable {
	
	private boolean cancelled;
	private final Selection newSelection;

	public BankResizeEvent(Player player, Bank bank, Selection newSelection) {
		super(player, bank);
		this.newSelection = newSelection;
	}
	
	public Selection getOldSelection() {
		return getBank().getSelection();
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
