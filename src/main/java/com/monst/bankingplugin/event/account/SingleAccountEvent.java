package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * An event involving a single {@link Account}.
 */
public abstract class SingleAccountEvent extends AccountEvent {

	private final Account account;

	protected SingleAccountEvent(CommandSender sender, Account account) {
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
