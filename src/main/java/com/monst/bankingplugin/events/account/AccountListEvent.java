package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This event is fired when a {@link CommandSender} fetches a list of accounts
 * using the {@link com.monst.bankingplugin.commands.account.AccountList} command.
 */
public class AccountListEvent extends MultiAccountEvent implements Cancellable {

    private boolean cancelled;

    public AccountListEvent(@Nonnull CommandSender sender, @Nonnull Collection<Account> accounts) {
        super(sender, accounts);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
