package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.events.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * Any event that involves one or more accounts.
 */
public abstract class AccountEvent extends BankingPluginEvent {

    public AccountEvent(CommandSender executor) {
        super(executor);
    }

}
