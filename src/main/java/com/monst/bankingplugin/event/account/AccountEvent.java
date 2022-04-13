package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.event.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * An event involving one or more accounts.
 */
public abstract class AccountEvent extends BankingPluginEvent {

    protected AccountEvent(CommandSender executor) {
        super(executor);
    }

}
