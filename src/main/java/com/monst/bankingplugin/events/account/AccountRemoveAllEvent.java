package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.account.AccountRemoveAll;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import java.util.Collection;

/**
 * This event is fired when multiple accounts are removed at once with the
 * {@link AccountRemoveAll} command.
 */
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
