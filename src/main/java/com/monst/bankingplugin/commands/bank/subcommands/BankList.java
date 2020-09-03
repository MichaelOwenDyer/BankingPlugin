package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.gui.BankListGui;
import com.monst.bankingplugin.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BankList extends BankSubCommand {

    public BankList() {
        super("list", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return Messages.COMMAND_USAGE_BANK_LIST;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is listing banks.");

        List<Bank> banks;

        // TODO: Allow for more specific bank searching

        banks = bankUtils.getBanksCopy().stream().sorted(Comparator.comparing(Bank::getTotalValue)).collect(Collectors.toList());

        if (banks.isEmpty()) {
            sender.sendMessage(String.format(Messages.NONE_FOUND, "banks", "list"));
            return true;
        }

        if (sender instanceof Player) {
            new BankListGui(banks).open(((Player) sender));
        } else {
            int i = 0;
            for (Bank bank : banks)
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + bank.getColorizedName() + " ");
        }
        return true;
    }

}
