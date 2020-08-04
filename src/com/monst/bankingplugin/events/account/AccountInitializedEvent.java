package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class AccountInitializedEvent extends MultiAccountEvent {

    public AccountInitializedEvent(Collection<Account> accounts) {
        super(null, accounts);
    }

}
