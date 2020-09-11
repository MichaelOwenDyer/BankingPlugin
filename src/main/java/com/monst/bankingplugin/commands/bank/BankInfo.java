package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.gui.BankGui;
import com.monst.bankingplugin.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankInfo extends BankCommand.SubCommand {

    BankInfo() {
        super("info", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return Messages.COMMAND_USAGE_BANK_INFO;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to show bank info");

        Bank bank = getBank(sender, args);
        if (bank == null)
            return true;

        plugin.debug(sender.getName() + " is displaying bank info");
        if (sender instanceof Player)
            new BankGui(bank).open((Player) sender);
        else
            sender.sendMessage(bank.getInformation());
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return bankUtils.getBanksCopy().stream()
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
