package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.BankingPluginCommand;
import com.monst.bankingplugin.lang.Message;

public class BankCommand extends BankingPluginCommand {

    public BankCommand(BankingPlugin plugin) {
        super(plugin, plugin.config().commandNames.bank.get(), Message.BANK_COMMAND_DESC,
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
		);
    }

}
