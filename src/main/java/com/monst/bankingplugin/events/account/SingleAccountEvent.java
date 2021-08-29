package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Any event that involves a single {@link Account}.
 */
public abstract class SingleAccountEvent extends AccountEvent {

	private final Account account;

	public SingleAccountEvent(CommandSender sender, Account account) {
		super(sender);
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

	public Player getPlayer() {
		return getExecutor() instanceof Player ? (Player) getExecutor() : null;
	}

}
