package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

public class AccountInfoEvent extends AccountEvent implements Cancellable {
	
	private boolean cancelled;
	private boolean verbose;

	public AccountInfoEvent(Player player, Account account, boolean verbose) {
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

	public boolean isVerbose() {
		return verbose;
	}

}
