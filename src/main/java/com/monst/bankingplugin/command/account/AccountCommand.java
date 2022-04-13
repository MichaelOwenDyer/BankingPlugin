package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.AbstractCommand;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;

public class AccountCommand extends AbstractCommand {

	public AccountCommand(BankingPlugin plugin) {
		super(plugin, plugin.config().accountCommandName.get(), Message.ACCOUNT_COMMAND_DESC,
				Arrays.asList(
						new AccountOpen(plugin),
						new AccountInfo(plugin),
						new AccountLimits(plugin),
						new AccountList(plugin),
						new AccountMigrate(plugin),
						new AccountRecover(plugin),
						new AccountClose(plugin),
						new AccountCloseAll(plugin),
						new AccountRename(plugin),
						new AccountConfigure(plugin),
						new AccountTransfer(plugin),
						new AccountTrust(plugin),
						new AccountUntrust(plugin)
				)
		);
	}

}
