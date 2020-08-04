package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

public class AccountCreateEvent extends AccountEvent implements Cancellable {
	
	private boolean cancelled;
	private OfflinePlayer newOwner;

	public AccountCreateEvent(Player player, OfflinePlayer newOwner, Account account) {
		super(player, account);
		this.newOwner = newOwner;
	}

	public boolean isForSelf() {
		return Utils.samePlayer(getPlayer(), newOwner);
	}

	public OfflinePlayer getNewOwner() {
		return newOwner;
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
