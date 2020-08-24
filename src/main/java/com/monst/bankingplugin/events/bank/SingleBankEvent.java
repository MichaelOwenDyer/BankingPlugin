package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import org.bukkit.command.CommandSender;

public abstract class SingleBankEvent extends BankEvent {

	private final Bank bank;
	
	public SingleBankEvent(CommandSender sender, Bank bank) {
		super(sender);
		this.bank = bank;
	}
	
	public Bank getBank() {
		return bank;
	}
	
}
