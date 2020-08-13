package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public abstract class MultiAccountEvent extends AccountEvent {

    private final Collection<Account> accounts;

    public MultiAccountEvent(CommandSender sender, Collection<Account> accounts) {
        super(sender);
        this.accounts = accounts;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

}
