package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.events.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * An event involving one or more accounts.
 */
public abstract class AccountEvent extends BankingPluginEvent {

    protected AccountEvent(CommandSender executor) {
        super(executor);
    }

}
