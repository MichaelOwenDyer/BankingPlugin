package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

/**
 * This event is fired when the contents of an account chest are updated in a way that the balance changes.
 */
public class AccountTransactionEvent extends SingleAccountEvent {

	private final BigDecimal amount;
	private final BigDecimal newBalance;

	public AccountTransactionEvent(Player player, Account account, BigDecimal amount, BigDecimal newBalance) {
		super(player, account);
		this.amount = amount;
		this.newBalance = newBalance;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getNewBalance() {
		return newBalance;
	}

	@Override
	public void fire() {
		super.callEvent();
	}

}
