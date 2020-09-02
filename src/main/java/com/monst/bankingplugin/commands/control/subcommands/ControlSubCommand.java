package com.monst.bankingplugin.commands.control.subcommands;

import com.monst.bankingplugin.commands.BankingPluginSubCommand;

public abstract class ControlSubCommand extends BankingPluginSubCommand {

    public ControlSubCommand(String name, boolean playerCommand) {
        super(name, playerCommand);
    }

}
