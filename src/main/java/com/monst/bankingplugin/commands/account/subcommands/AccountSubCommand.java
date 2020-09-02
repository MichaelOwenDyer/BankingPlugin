package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.utils.AccountUtils;

public abstract class AccountSubCommand extends BankingPluginSubCommand {

    final AccountUtils accountUtils = plugin.getAccountUtils();

    public AccountSubCommand(String name, boolean playerCommand) {
        super(name, playerCommand);
    }

}
