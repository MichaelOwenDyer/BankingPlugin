package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankSelectEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.external.BankVisualization;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankSelect extends PlayerSubCommand {

    BankSelect(BankingPlugin plugin) {
		super(plugin, "select");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_SELECT;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_SELECT;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_SELECT;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException {
        if (!plugin.isWorldEditIntegrated() && !plugin.isGriefPreventionIntegrated())
            throw err(Message.CANT_SELECT_BANK);

        Bank bank;
        if (args.length == 0) {
            bank = plugin.getBankService().findContaining(player);
            if (bank == null)
                throw err(Message.MUST_STAND_IN_OR_SPECIFY_BANK);
        } else {
            bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));
        }

        new BankSelectEvent(player, bank).fire();

        if (plugin.isWorldEditIntegrated())
            WorldEditReader.setSelection(plugin, bank.getRegion(), player);
        if (plugin.isGriefPreventionIntegrated())
            new BankVisualization(plugin, bank).show(player);
        plugin.debug(player.getName() + " has selected a bank");
        player.sendMessage(Message.BANK_SELECTED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return plugin.getBankService().findAllNames().stream()
                .filter(name -> containsIgnoreCase(name, args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

}
