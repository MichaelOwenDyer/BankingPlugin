package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.events.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * An event involving one or more banks.
 */
public abstract class BankEvent extends BankingPluginEvent {

    protected BankEvent(CommandSender executor) {
        super(executor);
    }

}
