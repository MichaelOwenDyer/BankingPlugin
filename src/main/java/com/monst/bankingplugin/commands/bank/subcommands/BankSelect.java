package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.external.WorldEditReader;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankSelect extends BankSubCommand {

    public BankSelect() {
        super("select", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.BANK_SELECT) ? Messages.COMMAND_USAGE_BANK_SELECT : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to select a bank");

        if (!plugin.hasWorldEdit()) {
            plugin.debug("WorldEdit is not enabled");
            p.sendMessage(Messages.WORLDEDIT_NOT_ENABLED);
            return true;
        }

        if (!p.hasPermission(Permissions.BANK_SELECT)) {
            plugin.debug(p.getName() + " does not have permission to select a bank");
            p.sendMessage(Messages.NO_PERMISSION_BANK_SELECT);
            return true;
        }

        Bank bank;
        if (args.length == 1) {
            bank = bankUtils.getBank(p.getLocation());
            if (bank == null) {
                plugin.debug(p.getName() + " wasn't standing in a bank");
                p.sendMessage(Messages.NOT_STANDING_IN_BANK);
                return true;
            }
        } else {
            bank = bankUtils.lookupBank(args[1]);
            if (bank == null) {
                plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
                p.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
                return true;
            }
        }

        WorldEditReader.setSelection(plugin, bank.getSelection(), p);
        plugin.debug(p.getName() + " has selected a bank");
        p.sendMessage(String.format(Messages.BANK_SELECTED,
                bank.getSelection().getType() == Selection.SelectionType.CUBOID ? "cuboid" : "polygon"));
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (!p.hasPermission(Permissions.BANK_SELECT))
            return Collections.emptyList();
        if (args.length == 2) {
            if (args[1].isEmpty() && bankUtils.isBank(p.getLocation()))
                return Collections.singletonList(bankUtils.getBank(p.getLocation()).getName());
            return bankUtils.getBanksCopy().stream()
                    .map(Bank::getName)
                    .sorted()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
