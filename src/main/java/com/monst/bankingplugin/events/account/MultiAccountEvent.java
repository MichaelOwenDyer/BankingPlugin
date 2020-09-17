package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Any event that involves multiple {@link Account}s.
 */
public abstract class MultiAccountEvent extends AccountEvent {

    private final Collection<Account> accounts;

    public MultiAccountEvent(@Nonnull CommandSender sender, @Nonnull Collection<Account> accounts) {
        super(sender);
        this.accounts = accounts;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

}
