package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;

/**
 * An event involving a single {@link Bank}.
 */
public abstract class SingleBankEvent extends BankEvent {

	private final Bank bank;

	protected SingleBankEvent(CommandSender sender, Bank bank) {
		super(sender);
		this.bank = bank;
	}

	public Bank getBank() {
		return bank;
	}

}
