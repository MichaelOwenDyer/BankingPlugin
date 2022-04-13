package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.event.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * An event involving one or more banks.
 */
public abstract class BankEvent extends BankingPluginEvent {

    protected BankEvent(CommandSender executor) {
        super(executor);
    }

}
