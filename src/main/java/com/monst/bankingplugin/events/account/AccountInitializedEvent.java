package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.Bukkit;

import java.util.Collection;

public class AccountInitializedEvent extends MultiAccountEvent {

    public AccountInitializedEvent(Collection<Account> accounts) {
        super(Bukkit.getConsoleSender(), accounts);
    }

}
