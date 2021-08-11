package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;
import java.util.List;

public class AccountCommand extends BankingPluginCommand {

	private static boolean commandCreated = false;

	public AccountCommand(BankingPlugin plugin) {
		super(plugin, Config.accountCommandName.get(), Message.ACCOUNT_COMMAND_DESC);
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return Arrays.asList(
				new AccountCreate(plugin),
				new AccountInfo(plugin),
				new AccountLimits(plugin),
				new AccountList(plugin),
				new AccountMigrate(plugin),
				new AccountRecover(plugin),
				new AccountRemove(plugin),
				new AccountRemoveAll(plugin),
				new AccountRename(plugin),
				new AccountConfigure(plugin),
				new AccountTransfer(plugin),
				new AccountTrust(plugin),
				new AccountUntrust(plugin)
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
