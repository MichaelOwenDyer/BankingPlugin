package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collection;

/**
 * This event is fired when a {@link CommandSender} fetches a list of accounts
 * using the {@link com.monst.bankingplugin.commands.account.AccountList} command.
 */
public class AccountListEvent extends MultiAccountEvent implements Cancellable {

    private boolean cancelled;

    public AccountListEvent(CommandSender sender, Collection<Account> accounts) {
        super(sender, accounts);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
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
