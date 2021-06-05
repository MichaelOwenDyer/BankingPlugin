package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.BankingPluginEvent;
import org.bukkit.command.CommandSender;

public abstract class ControlEvent extends BankingPluginEvent {

    public ControlEvent(BankingPlugin plugin, CommandSender sender) {
        super(sender);
    }

    public ControlEvent(CommandSender sender) {
        super(sender);
    }

    @Override
    public CommandSender getSender() {
        return super.getSender();
    }

}
