package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.control.InterestEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class ControlPayInterest extends ControlCommand.SubCommand {

    ControlPayInterest() {
        super("payinterest", false);
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
        PLUGIN.debug(sender.getName() + " is triggering an interest payout");

        if (!sender.hasPermission(Permissions.PAY_INTEREST)) {
            PLUGIN.debug(sender.getName() + " does not have permission to trigger an interest payout");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_PAY_INTEREST));
            return true;
        }

        Set<Bank> banks;
        if (args.length == 1)
            banks = PLUGIN.getBankRepository().getAll();
        else
            banks = Arrays.stream(args)
                    .map(PLUGIN.getBankRepository()::getByIdentifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        InterestEvent event = new InterestEvent(sender, banks);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Interest event cancelled");
            return true;
        }
        PLUGIN.debugf("%s has triggered an interest payment at %s", sender.getName(), Utils.map(banks, Bank::getName));
        sender.sendMessage(LangUtils.getMessage(Message.INTEREST_PAYOUT_TRIGGERED, new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size)));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.PAY_INTEREST))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return PLUGIN.getBankRepository().getAll().stream()
                .map(Bank::getName)
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
