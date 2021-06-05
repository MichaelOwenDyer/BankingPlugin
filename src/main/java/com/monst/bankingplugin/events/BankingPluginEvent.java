package com.monst.bankingplugin.events;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankingPluginEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

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

    public BankingPlugin getPlugin() {
        return PLUGIN;
    }

    protected CommandSender getSender() {
        return sender;
    }

    public void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
