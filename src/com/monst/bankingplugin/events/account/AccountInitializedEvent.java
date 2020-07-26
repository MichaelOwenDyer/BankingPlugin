package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class AccountInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Collection<Account> accounts;

    public AccountInitializedEvent(Collection<Account> accounts) {
        this.accounts = accounts;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
