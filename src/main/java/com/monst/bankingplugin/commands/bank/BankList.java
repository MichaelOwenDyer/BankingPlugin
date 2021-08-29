package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.gui.BankListGUI;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankList extends SubCommand.BankSubCommand {

    BankList(BankingPlugin plugin) {
		super(plugin, "list", true);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        plugin.debug(p.getName() + " is listing banks.");

        // TODO: Allow for specific bank searching

        new BankListGUI(plugin.getBankRepository()::getAll).open(p);
        return true;
    }

}
