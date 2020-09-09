package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class AccountTransactionEvent extends SingleAccountEvent {
	
	private final BigDecimal amount;
	private final BigDecimal newBalance;
	
	public AccountTransactionEvent(Player executor, Account account, BigDecimal amount, BigDecimal newBalance) {
		super(executor, account);
		this.amount = amount;
		this.newBalance = newBalance;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public BigDecimal getNewBalance() {
		return newBalance;
	}

}
