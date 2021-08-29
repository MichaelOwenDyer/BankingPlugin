package com.monst.bankingplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class BankingPluginEvent extends Event {

    private boolean fired = false;
    private final CommandSender executor;

    public BankingPluginEvent(CommandSender executor) {
        this.executor = executor;
    }

    public CommandSender getExecutor() {
        return executor;
    }

    public final void fire() {
        if (fired)
            throw new IllegalStateException("Event has already been called!");
        fired = true;
        Bukkit.getPluginManager().callEvent(this);
    }

}
