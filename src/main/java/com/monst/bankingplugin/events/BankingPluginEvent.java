package com.monst.bankingplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankingPluginEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender sender;

    public BankingPluginEvent(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    protected CommandSender getSender() {
        return sender;
    }

    public final void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
