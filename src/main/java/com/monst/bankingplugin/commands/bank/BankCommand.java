package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.stream.Stream;

public class BankCommand extends BankingPluginCommand<SubCommand.BankSubCommand> {

	private static boolean commandCreated = false;

    public BankCommand(final BankingPlugin plugin) {
        super(plugin, Config.bankCommandName.get(), Message.BANK_COMMAND_DESC);
    }

	@Override
	protected Stream<SubCommand.BankSubCommand> getSubCommands() {
		return Stream.of(
				new BankCreate(),
				new BankInfo(),
				new BankLimits(),
				new BankList(),
				new BankRemove(),
				new BankRemoveAll(),
				new BankRename(),
				new BankResize(),
				new BankSelect(),
				new BankConfigure(),
				new BankTransfer(),
				new BankTrust(),
				new BankUntrust()
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
