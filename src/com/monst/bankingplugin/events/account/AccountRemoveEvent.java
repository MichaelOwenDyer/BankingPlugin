package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

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
