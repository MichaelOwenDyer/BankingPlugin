package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.bank.BankSelectEvent;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankSelect extends SubCommand.BankSubCommand {

    BankSelect() {
        super("select", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_SELECT;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_SELECT;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        PLUGIN.debug(p.getName() + " wants to select a bank");

        if (!PLUGIN.isWorldEditIntegrated() && !PLUGIN.isGriefPreventionIntegrated()) {
            PLUGIN.debug("Cannot select bank. Neither WorldEdit nor GriefPrevention is enabled.");
            p.sendMessage(Messages.get(Message.CANT_SELECT_BANK));
            return true;
        }

        if (!p.hasPermission(Permissions.BANK_SELECT)) {
            PLUGIN.debug(p.getName() + " does not have permission to select a bank");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_SELECT));
            return true;
        }

        Bank bank;
        if (args.length == 1) {
            bank = bankRepo.getAt(p.getLocation().getBlock());
            if (bank == null) {
                PLUGIN.debug(p.getName() + " wasn't standing in a bank");
                p.sendMessage(Messages.get(Message.MUST_STAND_IN_BANK));
                return true;
            }
        } else {
            bank = bankRepo.getByIdentifier(args[1]);
            if (bank == null) {
                PLUGIN.debugf("Couldn't find bank with name or ID %s", args[1]);
                p.sendMessage(Messages.get(Message.BANK_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
                return true;
            }
        }

        BankSelectEvent event = new BankSelectEvent(p, bank);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Bank select event cancelled");
            return true;
        }

        if (PLUGIN.isWorldEditIntegrated())
            WorldEditReader.setSelection(PLUGIN, bank.getRegion(), p);
        PLUGIN.debug(p.getName() + " has selected a bank");
        p.sendMessage(Messages.get(Message.BANK_SELECTED,
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
        ));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(Permissions.BANK_SELECT)) {
            return bankRepo.getAll().stream()
                    .map(Bank::getName)
                    .sorted()
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
