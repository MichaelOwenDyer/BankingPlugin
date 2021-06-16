package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.repository.AccountRepository;

import java.util.stream.Stream;

public class AccountCommand extends BankingPluginCommand<AccountCommand.SubCommand> {

	private static boolean commandCreated = false;

	public AccountCommand(final BankingPlugin plugin) {

		super(plugin);

		if (commandCreated) {
			IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}

		this.name = Config.accountCommandName.get();
		this.desc = LangUtils.getMessage(Message.ACCOUNT_COMMAND_DESC);
		this.pluginCommand = super.createPluginCommand();

		Stream.of(
				new AccountCreate(),
				new AccountInfo(),
				new AccountLimits(),
				new AccountList(),
				new AccountMigrate(),
				new AccountRecover(),
				new AccountRemove(),
				new AccountRemoveAll(),
				new AccountRename(),
				new AccountConfigure(),
				new AccountTransfer(),
				new AccountTrust(),
				new AccountUntrust()
		).forEach(this::addSubCommand);

		register();
		commandCreated = true;

	}

	abstract static class SubCommand extends BankingPluginSubCommand {

		static final AccountRepository accountRepo = PLUGIN.getAccountRepository();

		SubCommand(String name, boolean playerCommand) {
			super(name, playerCommand);
		}

	}

}
