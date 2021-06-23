package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.Bukkit;

import java.util.Set;

/**
 * This event is fired when the plugin is started or reloaded.
 * It contains a reference to every account that was successfully loaded.
 */
public class AccountInitializedEvent extends MultiAccountEvent {

    public AccountInitializedEvent(Set<Account> accounts) {
        super(Bukkit.getConsoleSender(), accounts);
    }

}
