package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.entity.Player;

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
