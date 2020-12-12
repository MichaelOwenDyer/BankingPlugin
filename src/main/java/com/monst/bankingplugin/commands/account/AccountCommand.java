package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.AccountRepository;

public class AccountCommand extends BankingPluginCommand<AccountCommand.SubCommand> {

	private static boolean commandCreated = false;

	public AccountCommand(final BankingPlugin plugin) {

		super(plugin);

		if (commandCreated) {
			IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}

		this.name = Config.commandNameAccount;
		this.desc = LangUtils.getMessage(Message.ACCOUNT_COMMAND_DESC);
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new AccountCreate());
		addSubCommand(new AccountInfo());
		addSubCommand(new AccountLimits());
		addSubCommand(new AccountList());
		addSubCommand(new AccountMigrate());
		addSubCommand(new AccountRecover());
		addSubCommand(new AccountRemove());
		addSubCommand(new AccountRemoveall());
		addSubCommand(new AccountRename());
		addSubCommand(new AccountSet());
		addSubCommand(new AccountTransfer());
		addSubCommand(new AccountTrust());
		addSubCommand(new AccountUntrust());

		register();
		commandCreated = true;

	}

	abstract static class SubCommand extends BankingPluginSubCommand {

		static final AccountRepository accountRepo = plugin.getAccountRepository();

		SubCommand(String name, boolean playerCommand) {
			super(name, playerCommand);
		}

	}
}
