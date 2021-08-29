package com.monst.bankingplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class BankingPluginEvent extends Event {

    private final CommandSender sender;

    public BankingPluginEvent(CommandSender sender) {
        this.sender = sender;
    }

    protected CommandSender getSender() {
        return sender;
    }

    public final void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
