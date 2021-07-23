package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.stream.Stream;

public class AccountCommand extends BankingPluginCommand<SubCommand.AccountSubCommand> {

	private static boolean commandCreated = false;

	public AccountCommand(final BankingPlugin plugin) {
		super(plugin, Config.accountCommandName.get(), Message.ACCOUNT_COMMAND_DESC);
	}

	@Override
	protected Stream<SubCommand.AccountSubCommand> getSubCommands() {
		return Stream.of(
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
		);
	}

	@Override
	protected boolean isCreated() {
		return commandCreated;
	}

	@Override
	protected void setCreated() {
		commandCreated = true;
	}

}
