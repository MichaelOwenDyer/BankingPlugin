package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.BankingPluginCommand;
import com.monst.bankingplugin.lang.Message;

public class AccountCommand extends BankingPluginCommand {

	public AccountCommand(BankingPlugin plugin) {
		super(plugin, plugin.config().commandNames.account.get(), Message.ACCOUNT_COMMAND_DESC,
				new AccountOpen(plugin),
				new AccountInfo(plugin),
				new AccountLimits(plugin),
				new AccountList(plugin),
				new AccountRecover(plugin),
				new AccountMigrate(plugin),
				new AccountClose(plugin),
				new AccountCloseAll(plugin),
				new AccountRename(plugin),
				new AccountConfigure(plugin),
				new AccountTransfer(plugin),
				new AccountTrust(plugin),
				new AccountUntrust(plugin)
		);
	}

}
