package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any event that involves a single {@link Account}.
 */
public abstract class SingleAccountEvent extends AccountEvent {

	private final Account account;

	public SingleAccountEvent(@Nonnull CommandSender sender, @Nonnull Account account) {
		super(sender);
		this.account = account;
	}

	@Nonnull
	public Account getAccount() {
		return account;
	}

	@Nullable
	public Player getPlayer() {
		return getExecutor() instanceof Player ? (Player) getExecutor() : null;
	}

}
