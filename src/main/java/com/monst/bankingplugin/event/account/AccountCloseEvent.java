package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when an account is removed by a player.
 */
public class AccountCloseEvent extends SingleAccountEvent {

	public AccountCloseEvent(Player player, Account account) {
		super(player, account);
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public void fire() {
		super.callEvent();
	}

}
