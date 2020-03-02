package com.monst.bankingplugin.events.account;

import java.math.BigDecimal;

import org.bukkit.entity.Player;

import com.monst.bankingplugin.Account;

public class AccountTransactionEvent extends AccountEvent {
	
	private TransactionType type;
	private BigDecimal amount;
	private BigDecimal newBalance;
	
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
	
	public enum TransactionType {
		DEPOSIT, WITHDRAWAL
	}

}
