package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankUntrust extends BankSubCommand {

    public BankUntrust() {
        super("untrust", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.BANK_TRUST) ? Messages.COMMAND_USAGE_BANK_UNTRUST : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return false;

        plugin.debug(sender.getName() + " wants to untrust a player from a bank");

        if (!sender.hasPermission(Permissions.BANK_TRUST)) {
            sender.sendMessage(Messages.NO_PERMISSION_BANK_UNTRUST);
            return true;
        }
        Bank bank = plugin.getBankUtils().lookupBank(args[1]);
        if (bank == null) {
            sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
            return true;
        }
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[2]);
        if (playerToUntrust == null) {
            sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
            return true;
        }

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRUST_OTHER))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
                plugin.debugf("%s does not have permission to untrust a player from bank %s as a co-owner",
                        sender.getName(), bank.getName());
                sender.sendMessage(Messages.MUST_BE_OWNER);
                return true;
            }
            plugin.debugf("%s does not have permission to untrust a player from bank %s", sender.getName(), bank.getName());
            sender.sendMessage(Messages.NO_PERMISSION_BANK_UNTRUST_OTHER);
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRUST_ADMIN)) {
            plugin.debugf("%s does not have permission to untrust a player from admin bank %s", sender.getName(), bank.getName());
            sender.sendMessage(Messages.NO_PERMISSION_BANK_UNTRUST_ADMIN);
            return true;
        }

        boolean isSelf = sender instanceof Player && Utils.samePlayer(playerToUntrust, ((Player) sender));
        if (!bank.isCoowner(playerToUntrust)) {
            plugin.debugf("%s was not co-owner at bank %s (#%d)", playerToUntrust.getName(), bank.getName(), bank.getID());
            sender.sendMessage(String.format(Messages.NOT_A_COOWNER,
                    isSelf ? "You are" : playerToUntrust.getName() + " is", "bank"));
            return true;
        }

        plugin.debugf("%s has untrusted %s from bank %s (#%d)",
                sender.getName(), playerToUntrust.getName(), bank.getName(), bank.getID());
        sender.sendMessage(String.format(Messages.REMOVED_COOWNER, isSelf ? "You were" : playerToUntrust.getName() + " was"));
        bank.untrustPlayer(playerToUntrust);
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankUtils.getBanksCopy().stream()
                    .filter(bank -> bank.isOwner(p)
                            || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRUST_OTHER))
                            || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRUST_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            Bank bank = plugin.getBankUtils().lookupBank(args[1]);
            if (bank == null)
                return Collections.emptyList();
            List<String> coowners = bank.getCoowners().stream().map(OfflinePlayer::getName).collect(Collectors.toList());
            return Utils.filter(coowners, name -> name.toLowerCase().startsWith(args[2].toLowerCase()));
        }
        return Collections.emptyList();
    }

}
