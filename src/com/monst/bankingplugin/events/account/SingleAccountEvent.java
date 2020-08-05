package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;

import com.monst.bankingplugin.Account;

public abstract class SingleAccountEvent extends AccountEvent {
	
	private final Account account;
	
	public SingleAccountEvent(Player player, Account account) {
		super(player);
		this.account = account;
	}
	
	public Player getPlayer() {
		return (Player) getExecutor();
	}
	
	public Account getAccount() {
		return account;
	}
	
}
