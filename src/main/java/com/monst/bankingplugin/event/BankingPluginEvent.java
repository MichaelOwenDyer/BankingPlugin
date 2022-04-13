package com.monst.bankingplugin.event;

import com.monst.bankingplugin.exception.CancelledException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class BankingPluginEvent extends Event implements CommandSenderEvent {

    private final CommandSender executor;

    protected BankingPluginEvent(CommandSender executor) {
        this.executor = executor;
    }

    @Override
    public CommandSender getExecutor() {
        return executor;
    }

    public void fire() throws CancelledException {
        callEvent();
        if (this instanceof Cancellable && ((Cancellable) this).isCancelled())
            throw new CancelledException();
    }

    protected void callEvent() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
