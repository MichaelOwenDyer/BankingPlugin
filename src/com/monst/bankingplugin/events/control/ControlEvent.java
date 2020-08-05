package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ControlEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final BankingPlugin plugin;
    private final CommandSender sender;

    public ControlEvent(BankingPlugin plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BankingPlugin getPlugin() {
        return plugin;
    }

    public CommandSender getSender() {
        return sender;
    }
}
