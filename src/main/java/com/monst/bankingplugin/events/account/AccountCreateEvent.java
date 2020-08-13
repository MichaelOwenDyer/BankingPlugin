package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountCreateEvent extends SingleAccountEvent implements Cancellable {
	
	private boolean cancelled;
	private final OfflinePlayer accountOwner;

	public AccountCreateEvent(Player executor, OfflinePlayer accountOwner, Account account) {
		super(executor, account);
		this.accountOwner = accountOwner;
	}

	public boolean isForSelf() {
		return Utils.samePlayer(getPlayer(), accountOwner);
	}

	public OfflinePlayer getAccountOwner() {
		return accountOwner;
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
