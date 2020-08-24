package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountRemoveEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountRemoveEvent(Player player, Account account) {
		super(player, account);
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
