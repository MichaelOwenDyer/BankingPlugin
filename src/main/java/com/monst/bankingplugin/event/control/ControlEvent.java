package com.monst.bankingplugin.event.control;

import com.monst.bankingplugin.event.BankingPluginEvent;
import org.bukkit.command.CommandSender;

/**
 * An event involving the control of BankingPlugin.
 */
public abstract class ControlEvent extends BankingPluginEvent {

    protected ControlEvent(CommandSender sender) {
        super(sender);
    }

}
