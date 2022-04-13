package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.AbstractCommand;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;

public class BankCommand extends AbstractCommand {

    public BankCommand(BankingPlugin plugin) {
        super(plugin, plugin.config().bankCommandName.get(), Message.BANK_COMMAND_DESC,
				Arrays.asList(
						new BankCreate(plugin),
						new BankInfo(plugin),
						new BankLimits(plugin),
						new BankList(plugin),
						new BankRemove(plugin),
						new BankRemoveAll(plugin),
						new BankRename(plugin),
						new BankResize(plugin),
						new BankSelect(plugin),
						new BankConfigure(plugin),
						new BankTransfer(plugin),
						new BankTrust(plugin),
						new BankUntrust(plugin)
				)
		);
    }

}
