package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.command.CommandSender;

import java.util.Collection;

/**
 * An event involving multiple {@link Account}s.
 */
public abstract class MultiAccountEvent extends AccountEvent {

    private final Collection<Account> accounts;

    protected MultiAccountEvent(CommandSender sender, Collection<Account> accounts) {
        super(sender);
        this.accounts = accounts;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

}
