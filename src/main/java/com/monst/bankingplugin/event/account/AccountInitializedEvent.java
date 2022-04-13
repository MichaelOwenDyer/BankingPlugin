package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.Set;

/**
 * This event is fired when the plugin is started or reloaded.
 * It contains a reference to every account that was successfully loaded.
 */
public class AccountInitializedEvent extends MultiAccountEvent {

    public AccountInitializedEvent(Set<Account> accounts) {
        super(Bukkit.getConsoleSender(), accounts);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
