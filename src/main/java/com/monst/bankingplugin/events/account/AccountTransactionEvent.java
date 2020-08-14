package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.listeners.AccountBalanceListener.TransactionType;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class AccountTransactionEvent extends SingleAccountEvent {
	
	private final TransactionType type;
	private final BigDecimal amount;
	private final BigDecimal newBalance;
	
	public AccountTransactionEvent(Player executor, Account account, TransactionType type, BigDecimal amount,
			BigDecimal newBalance) {
		super(executor, account);
		this.type = type;
		this.amount = amount;
		this.newBalance = newBalance;
	}
	
	public TransactionType getType() {
		return type;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public BigDecimal getNewBalance() {
		return newBalance;
	}

}