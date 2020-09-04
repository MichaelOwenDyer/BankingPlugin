package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.bank.subcommands.*;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;

public class BankCommand extends BankingPluginCommand<BankSubCommand> {

	private static boolean commandCreated = false;

    public BankCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.mainCommandNameBank + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.mainCommandNameBank;
        this.desc = Messages.BANK_COMMAND_DESC;
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new BankCreate());
		addSubCommand(new BankRemove());
		addSubCommand(new BankInfo());
		addSubCommand(new BankList());
		addSubCommand(new BankLimits());
		addSubCommand(new BankRemoveall());
		addSubCommand(new BankResize());
		addSubCommand(new BankRename());
		addSubCommand(new BankSet());
		addSubCommand(new BankTrust());
		addSubCommand(new BankUntrust());
		addSubCommand(new BankSelect());
		addSubCommand(new BankTransfer());

        register();
        commandCreated = true;

    }

}
