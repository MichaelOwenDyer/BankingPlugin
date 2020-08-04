package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class MultiBankEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private final Collection<Bank> banks;

    public MultiBankEvent(CommandSender sender, Collection<Bank> banks) {
        this.sender = sender;
        this.banks = banks;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Collection<Bank> getBanks() {
        return banks;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
