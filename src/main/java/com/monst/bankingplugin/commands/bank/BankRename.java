package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
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
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_RENAME;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        PLUGIN.debug(sender.getName() + " is renaming a bank");

        if (args.length < 2)
            return false;

        Bank bank;
        StringBuilder sb;
        String newName;
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                PLUGIN.debug("Must be player");
                sender.sendMessage(LangUtils.getMessage(Message.PLAYER_COMMAND_ONLY));
                return true;
            }
            bank = bankRepo.getAt(((Player) sender).getLocation());
            if (bank == null) {
                PLUGIN.debug(sender.getName() + " was not standing in a bank");
                sender.sendMessage(LangUtils.getMessage(Message.MUST_STAND_IN_BANK));
                return true;
            }
            newName = args[1];
        } else {
            bank = bankRepo.getByIdentifier(args[1]);
            if (bank == null) {
                PLUGIN.debugf("Couldn't find bank with name or ID %s", args[1]);
                sender.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
                return true;
            }
            sb = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; i++)
                sb.append(" ").append(args[i]);
            newName = sb.toString();
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            PLUGIN.debug(sender.getName() + " does not have permission to change the name of an admin bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_SET_ADMIN));
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            PLUGIN.debug(sender.getName() + " does not have permission to change the name of another player's bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_SET_OTHER));
            return true;
        }
        if (bank.getRawName().contentEquals(newName)) {
            PLUGIN.debug("Same name");
            sender.sendMessage(LangUtils.getMessage(Message.NAME_NOT_CHANGED, new Replacement(Placeholder.BANK_NAME, newName)));
            return true;
        }
        Bank bankWithSameName = bankRepo.getByName(newName);
        if (bankWithSameName != null && !bankWithSameName.equals(bank)) {
            PLUGIN.debug("Name is not unique");
            sender.sendMessage(LangUtils.getMessage(Message.NAME_NOT_UNIQUE, new Replacement(Placeholder.BANK_NAME, newName)));
            return true;
        }
        if (!Utils.isAllowedName(newName)) {
            PLUGIN.debug("Name is not allowed");
            sender.sendMessage(LangUtils.getMessage(Message.NAME_NOT_ALLOWED, new Replacement(Placeholder.BANK_NAME, newName)));
            return true;
        }

        PLUGIN.debug(sender.getName() + " is changing the name of bank " + bank.getName() + " to " + newName);
        sender.sendMessage(LangUtils.getMessage(Message.NAME_CHANGED, new Replacement(Placeholder.BANK_NAME, newName)));
        bank.setName(newName);
        PLUGIN.getBankRepository().update(bank, Callback.blank(), BankField.NAME); // Update bank in database
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Bank bank = sender instanceof Player ? bankRepo.getAt(((Player) sender).getLocation()) : null;
            if (args[1].isEmpty() && bank != null)
                return Collections.singletonList(bank.getName());

            return bankRepo.getAll().stream()
                    .filter(b -> ((sender instanceof Player && b.isTrusted((Player) sender))
                            || (b.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                            || (b.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN))))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[1]))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
