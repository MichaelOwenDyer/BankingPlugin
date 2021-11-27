package com.monst.bankingplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class BankingPluginEvent extends Event implements CommandSenderEvent {

    private boolean fired = false;
    private final CommandSender executor;

    protected BankingPluginEvent(CommandSender executor) {
        this.executor = executor;
    }

    @Override
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
