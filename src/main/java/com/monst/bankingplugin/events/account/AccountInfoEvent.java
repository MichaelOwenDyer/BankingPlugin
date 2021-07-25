package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.gui.AccountGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when info about an {@link Account} is fetched.
 * This can be either a player opening an {@link AccountGUI} or the console printing out
 * an info dump and the command "account info [id]".
 */
public class AccountInfoEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountInfoEvent(@Nonnull CommandSender sender, @Nonnull Account account) {
		super(sender, account);
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
