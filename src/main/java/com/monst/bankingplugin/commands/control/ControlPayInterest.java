package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ControlPayInterest extends SubCommand {

    ControlPayInterest(BankingPlugin plugin) {
		super(plugin, "payinterest", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.UPDATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_PAY_INTEREST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is triggering an interest payout");

        if (!sender.hasPermission(Permissions.PAY_INTEREST)) {
            plugin.debug(sender.getName() + " does not have permission to trigger an interest payout");
            sender.sendMessage(Message.NO_PERMISSION_PAY_INTEREST.translate());
            return true;
        }

        Set<Bank> banks;
        if (args.length == 1)
            banks = plugin.getBankRepository().getAll();
        else
            banks = Arrays.stream(args)
                    .skip(1)
                    .map(plugin.getBankRepository()::getByIdentifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        plugin.debugf("%s has triggered an interest payment at %s", sender.getName(), Utils.map(banks, Bank::getName));
        sender.sendMessage(Message.INTEREST_PAYOUT_TRIGGERED.with(Placeholder.NUMBER_OF_BANKS).as(banks.size()).translate());

        InterestEvent event = new InterestEvent(sender, banks);
        event.fire();
        if (event.isCancelled())
            plugin.debug("Interest event cancelled");
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (!player.hasPermission(Permissions.PAY_INTEREST))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return plugin.getBankRepository().getAll().stream()
                .map(Bank::getName)
                .filter(name -> !argList.contains(name))
                .filter(name -> Utils.startsWithIgnoreCase(name, args[args.length - 1]))
                .sorted()
                .collect(Collectors.toList());
    }

}
