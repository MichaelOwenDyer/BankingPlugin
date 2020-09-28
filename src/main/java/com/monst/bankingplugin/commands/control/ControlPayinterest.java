package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class ControlPayinterest extends ControlCommand.SubCommand {

    ControlPayinterest() {
        super("payinterest", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.UPDATE) ? Messages.COMMAND_USAGE_PAY_INTEREST : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is triggering an interest payout");

        if (!sender.hasPermission(Permissions.PAY_INTEREST)) {
            plugin.debug(sender.getName() + " does not have permission to trigger an interest payout");
            sender.sendMessage(Messages.NO_PERMISSION_PAY_INTEREST);
            return true;
        }

        Set<Bank> banks;
        if (args.length == 1)
            banks = plugin.getBankUtils().getBanks();
        else
            banks = Arrays.stream(args)
                    .map(plugin.getBankUtils()::getBank)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        InterestEvent event = new InterestEvent(plugin, sender, banks);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Interest event cancelled");
            return true;
        }
        plugin.debugf(Messages.INTEREST_PAYOUT_TRIGGERED, banks.size(), banks.size() == 1 ? "" : "s");
        sender.sendMessage(String.format(Messages.INTEREST_PAYOUT_TRIGGERED, banks.size(), banks.size() == 1 ? "" : "s"));
        return true;
    }

    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.PAY_INTEREST))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return plugin.getBankUtils().getBanks().stream()
                .map(Bank::getName)
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
