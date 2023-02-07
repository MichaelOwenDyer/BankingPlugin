package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.gui.BankListGUI;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.entity.Player;

public class BankList extends PlayerSubCommand {

    BankList(BankingPlugin plugin) {
		super(plugin, "list");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIST;
    }

    @Override
    protected void execute(Player player, String[] args) {
        new BankListGUI(player, plugin).open();
    }

}
