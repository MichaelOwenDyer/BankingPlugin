package com.monst.bankingplugin.events.account;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.Arrays;

public abstract class AccountCommandEvent extends AccountEvent {

    private final String[] args;

    public AccountCommandEvent(CommandSender executor, String[] args) {
        super(executor);
        this.args = Arrays.copyOf(args, args.length);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String[] getArgs() {
        return args;
    }

}
