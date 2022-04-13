package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.control.InterestEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class BPPayInterest extends SubCommand {

    BPPayInterest(BankingPlugin plugin) {
		super(plugin, "payinterest");
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_PAY_INTEREST;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_PAY_INTEREST;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CancelledException {
        List<Bank> banks;
        if (args.length == 0)
            banks = plugin.getBankService().findAll();
        else
            banks = plugin.getBankService().findByNameIn(Arrays.asList(args));

        plugin.debugf("%s has triggered an interest payment at %s", sender.getName(), banks.stream().map(Bank::getName).collect(Collectors.toList()));
        sender.sendMessage(Message.INTEREST_PAYOUT_TRIGGERED.with(Placeholder.NUMBER_OF_BANKS).as(banks.size()).translate(plugin));

        new InterestEvent(sender, new HashSet<>(banks)).fire();
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        List<String> argList = Arrays.asList(args);
        return plugin.getBankService().findAll().stream()
                .map(Bank::getName)
                .filter(name -> !argList.contains(name))
                .filter(name -> Utils.startsWithIgnoreCase(name, args[args.length - 1]))
                .sorted()
                .collect(Collectors.toList());
    }

}
