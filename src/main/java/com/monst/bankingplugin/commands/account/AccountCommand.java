package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.account.subcommands.*;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;

public class AccountCommand extends BankingPluginCommand<AccountSubCommand> {

	private static boolean commandCreated = false;

	public AccountCommand(final BankingPlugin plugin) {

		super(plugin);

		if (commandCreated) {
			IllegalStateException e = new IllegalStateException(
					"Command \"" + Config.mainCommandNameAccount + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}

		this.name = Config.mainCommandNameAccount;
		this.desc = Messages.ACCOUNT_COMMAND_DESC;
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new AccountCreate());
		addSubCommand(new AccountRemove());
		addSubCommand(new AccountInfo());
		addSubCommand(new AccountList());
		addSubCommand(new AccountLimits());
		addSubCommand(new AccountRemoveall());
		addSubCommand(new AccountSet());
		addSubCommand(new AccountTrust());
		addSubCommand(new AccountUntrust());
		addSubCommand(new AccountMigrate());
		addSubCommand(new AccountTransfer());
		addSubCommand(new AccountRecover());

		register();
		commandCreated = true;
	}

}
