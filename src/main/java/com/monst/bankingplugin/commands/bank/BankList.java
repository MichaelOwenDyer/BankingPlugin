package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.gui.BankListGui;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BankList extends BankCommand.SubCommand {

    BankList() {
        super("list", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return LangUtils.getMessage(Message.COMMAND_USAGE_BANK_LIST, getReplacement());
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is listing banks.");

        // TODO: Allow for more specific bank searching

        Supplier<List<Bank>> getBanks = () -> bankUtils.getBanks().stream()
                .sorted(Comparator.comparing(Bank::getTotalValue).reversed())
                .collect(Collectors.toList());

        List<Bank> banks = getBanks.get();

        if (banks.isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.BANKS_NOT_FOUND));
            return true;
        }

        if (sender instanceof Player) {
            new BankListGui(getBanks).open(((Player) sender));
        } else {
            int i = 0;
            for (Bank bank : banks)
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + bank.getColorizedName() + " ");
        }
        return true;
    }

}
