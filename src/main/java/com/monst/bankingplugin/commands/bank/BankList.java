package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.gui.BankListGUI;
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
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is listing banks.");

        // TODO: Allow for more specific bank searching

        Supplier<List<Bank>> getBanks = () -> bankRepo.getAll().stream()
                .sorted(Comparator.comparing(Bank::getTotalValue).reversed())
                .collect(Collectors.toList());

        List<Bank> banks = getBanks.get();

        if (banks.isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.BANKS_NOT_FOUND));
            return true;
        }

        if (sender instanceof Player) {
            new BankListGUI(getBanks).open(((Player) sender));
        } else {
            int i = 0;
            for (Bank bank : banks)
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + bank.getColorizedName() + " ");
        }
        return true;
    }

}
