package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRename extends SubCommand.BankSubCommand {

    BankRename(BankingPlugin plugin) {
		super(plugin, "rename", false);
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
        plugin.debug(sender.getName() + " is renaming a bank");

        if (args.length < 2)
            return false;

        Bank bank;
        StringBuilder sb;
        String newName;
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                plugin.debug("Must be player");
                sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate());
                return true;
            }
            bank = plugin.getBankRepository().getAt(((Player) sender).getLocation().getBlock());
            if (bank == null) {
                plugin.debug(sender.getName() + " was not standing in a bank");
                sender.sendMessage(Message.MUST_STAND_IN_BANK.translate());
                return true;
            }
            newName = args[1];
        } else {
            bank = plugin.getBankRepository().getByIdentifier(args[1]);
            if (bank == null) {
                plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
                sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
                return true;
            }
            sb = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; i++)
                sb.append(" ").append(args[i]);
            newName = sb.toString();
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to change the name of an admin bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_SET_ADMIN.translate());
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            plugin.debug(sender.getName() + " does not have permission to change the name of another player's bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_SET_OTHER.translate());
            return true;
        }
        if (bank.getRawName().contentEquals(newName)) {
            plugin.debug("Same name");
            sender.sendMessage(Message.NAME_NOT_CHANGED.with(Placeholder.NAME).as(newName).translate());
            return true;
        }
        Bank bankWithSameName = plugin.getBankRepository().getByName(newName);
        if (bankWithSameName != null && !bankWithSameName.equals(bank)) {
            plugin.debug("Name is not unique");
            sender.sendMessage(Message.NAME_NOT_UNIQUE.with(Placeholder.NAME).as(newName).translate());
            return true;
        }
        if (!Config.nameRegex.matches(newName)) {
            plugin.debug("Name is not allowed");
            sender.sendMessage(Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(newName)
                    .and(Placeholder.PATTERN).as(Config.nameRegex.get())
                    .translate());
            return true;
        }

        plugin.debug(sender.getName() + " is changing the name of bank " + bank.getName() + " to " + newName);
        sender.sendMessage(Message.NAME_CHANGED.with(Placeholder.BANK_NAME).as(newName).translate());
        bank.setName(newName);
        plugin.getBankRepository().update(bank, BankField.NAME); // Update bank in database
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            Bank bank = plugin.getBankRepository().getAt((player).getLocation().getBlock());
            if (args[0].isEmpty() && bank != null)
                return Collections.singletonList(bank.getName());

            return plugin.getBankRepository().getAll().stream()
                    .filter(b -> b.isTrusted(player)
                            || (b.isPlayerBank() && player.hasPermission(Permissions.BANK_SET_OTHER))
                            || (b.isAdminBank() && player.hasPermission(Permissions.BANK_SET_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
