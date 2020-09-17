package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This event is fired when the plugin is started or reloaded.
 * It contains a reference to every account that was successfully loaded.
 */
public class AccountInitializedEvent extends MultiAccountEvent {

    public AccountInitializedEvent(@Nonnull Collection<Account> accounts) {
        super(Bukkit.getConsoleSender(), accounts);
    }

}
