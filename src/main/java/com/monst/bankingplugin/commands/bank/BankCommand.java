package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;
import java.util.List;

public class BankCommand extends BankingPluginCommand {

	private static boolean commandCreated = false;

    public BankCommand(BankingPlugin plugin) {
        super(plugin, Config.bankCommandName.get(), Message.BANK_COMMAND_DESC);
    }

	@Override
	protected List<SubCommand> getSubCommands() {
		return Arrays.asList(
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

	@Override
	protected boolean isCreated() {
		return commandCreated;
	}

	@Override
	protected void setCreated() {
		commandCreated = true;
	}

}
