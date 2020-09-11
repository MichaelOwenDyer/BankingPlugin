package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRename extends BankCommand.SubCommand {

    BankRename() {
        super("rename", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_RENAME : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is renaming a bank");

        if (args.length < 2)
            return false;

        Bank bank;
        StringBuilder sb;
        String newName;
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                plugin.debug("Must be player");
                sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
                return true;
            }
            bank = bankUtils.getBank(((Player) sender).getLocation());
            if (bank == null) {
                plugin.debug(sender.getName() + " was not standing in a bank");
                sender.sendMessage(Messages.NOT_STANDING_IN_BANK);
                return true;
            }
            newName = args[1];
        } else {
            bank = bankUtils.lookupBank(args[1]);
            if (bank == null) {
                plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
                sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
                return true;
            }
            sb = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; i++)
                sb.append(" ").append(args[i]);
            newName = sb.toString();
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to change the name of an admin bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_ADMIN);
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            plugin.debug(sender.getName() + " does not have permission to change the name of another player's bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_OTHER);
            return true;
        }
        if (bank.getName().contentEquals(newName)) {
            plugin.debug("Same name");
            sender.sendMessage(Messages.NAME_ALREADY);
            return true;
        }
        if (!bankUtils.isUniqueNameIgnoring(newName, bank.getName())) {
            plugin.debug("Name is not unique");
            sender.sendMessage(Messages.NAME_NOT_UNIQUE);
            return true;
        }
        if (!Utils.isAllowedName(newName)) {
            plugin.debug("Name is not allowed");
            sender.sendMessage(Messages.NAME_NOT_ALLOWED);
            return true;
        }

        plugin.debug(sender.getName() + " is changing the name of bank " + bank.getName() + " to " + newName);
        sender.sendMessage(Messages.NAME_CHANGED);
        bank.setName(newName);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Bank bank = sender instanceof Player ? bankUtils.getBank(((Player) sender).getLocation()) : null;
            if (args[1].isEmpty() && bank != null)
                return Collections.singletonList(bank.getName());

            return bankUtils.getBanksCopy().stream()
                    .filter(b -> ((sender instanceof Player && b.isTrusted((Player) sender))
                            || (b.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                            || (b.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN))))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
