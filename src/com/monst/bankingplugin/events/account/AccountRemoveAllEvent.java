package com.monst.bankingplugin.events.account;

import java.util.Collection;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.monst.bankingplugin.Account;

public class AccountRemoveAllEvent extends MultiAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountRemoveAllEvent(CommandSender sender, Collection<Account> accounts) {
		super(sender, accounts);
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
