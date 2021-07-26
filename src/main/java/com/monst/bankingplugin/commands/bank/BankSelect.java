package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.bank.BankSelectEvent;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankSelect extends SubCommand.BankSubCommand {

    BankSelect(BankingPlugin plugin) {
		super(plugin, "select", true);
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
        plugin.debug(p.getName() + " wants to select a bank");

        if (!plugin.isWorldEditIntegrated() && !plugin.isGriefPreventionIntegrated()) {
            plugin.debug("Cannot select bank. Neither WorldEdit nor GriefPrevention is enabled.");
            p.sendMessage(Message.CANT_SELECT_BANK.translate());
            return true;
        }

        if (!p.hasPermission(Permissions.BANK_SELECT)) {
            plugin.debug(p.getName() + " does not have permission to select a bank");
            p.sendMessage(Message.NO_PERMISSION_BANK_SELECT.translate());
            return true;
        }

        Bank bank;
        if (args.length == 1) {
            bank = plugin.getBankRepository().getAt(p.getLocation().getBlock());
            if (bank == null) {
                plugin.debug(p.getName() + " wasn't standing in a bank");
                p.sendMessage(Message.MUST_STAND_IN_BANK.translate());
                return true;
            }
        } else {
            bank = plugin.getBankRepository().getByIdentifier(args[1]);
            if (bank == null) {
                plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
                p.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
                return true;
            }
        }

        BankSelectEvent event = new BankSelectEvent(p, bank);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Bank select event cancelled");
            return true;
        }

        if (plugin.isWorldEditIntegrated())
            WorldEditReader.setSelection(plugin, bank.getRegion(), p);
        plugin.debug(p.getName() + " has selected a bank");
        p.sendMessage(Message.BANK_SELECTED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1 && player.hasPermission(Permissions.BANK_SELECT))
            return plugin.getBankRepository().getAll().stream()
                    .map(Bank::getName)
                    .sorted()
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
