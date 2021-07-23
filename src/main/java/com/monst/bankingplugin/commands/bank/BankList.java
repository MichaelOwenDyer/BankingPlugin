package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.gui.BankListGUI;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankList extends SubCommand.BankSubCommand {

    BankList() {
        super("list", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        PLUGIN.debug(sender.getName() + " is listing banks.");

        // TODO: Allow for more specific bank searching

        if (bankRepo.getAll().isEmpty()) {
            sender.sendMessage(Messages.get(Message.BANKS_NOT_FOUND));
            return true;
        }

        if (sender instanceof Player)
            new BankListGUI(bankRepo::getAll).open((Player) sender);
        else {
            int i = 0;
            for (Bank bank : bankRepo.getAll())
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + bank.getColorizedName() + " ");
        }
        return true;
    }

}
