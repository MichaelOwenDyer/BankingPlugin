package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.Arrays;

/**
 * This event is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountTransfer} command.
 */
public class AccountPreTransferEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
	private final String[] args;

	public AccountPreTransferEvent(Player player, String[] args) {
		super(player, null);
		this.args = args;
	}

	public String[] getArgs() {
		return Arrays.copyOf(args, args.length);
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
