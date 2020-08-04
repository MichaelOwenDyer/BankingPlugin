package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class MultiAccountEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private final Collection<Account> accounts;

    public MultiAccountEvent(CommandSender sender, Collection<Account> accounts) {
        this.sender = sender;
        this.accounts = accounts;
    }

    public CommandSender getSender() {
        return sender;
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
