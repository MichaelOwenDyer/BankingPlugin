package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.gui.BankGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankInfo extends SubCommand.BankSubCommand {

    BankInfo(BankingPlugin plugin) {
		super(plugin, "info", true);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_INFO;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        Bank bank = getBank(p, args);
        if (bank == null)
            return true;
        plugin.debugf("%s is viewing GUI of bank #%d", p.getName(), bank.getID());
        new BankGUI(bank).open(p);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
