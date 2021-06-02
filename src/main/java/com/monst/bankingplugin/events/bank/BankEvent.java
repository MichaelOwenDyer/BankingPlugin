package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.events.BankingPluginEvent;
import org.bukkit.command.CommandSender;

public abstract class BankEvent extends BankingPluginEvent {

    public BankEvent(CommandSender executor) {
        super(executor);
    }

    public CommandSender getExecutor() {
        return super.getSender();
    }

}
