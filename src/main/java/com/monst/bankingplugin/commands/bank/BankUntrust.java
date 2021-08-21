package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankUntrust extends SubCommand.BankSubCommand {

    BankUntrust(BankingPlugin plugin) {
		super(plugin, "untrust", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_UNTRUST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return false;

        plugin.debug(sender.getName() + " wants to untrust a player from a bank");

        if (!sender.hasPermission(Permissions.BANK_TRUST)) {
            sender.sendMessage(Message.NO_PERMISSION_BANK_UNTRUST.translate());
            return true;
        }
        Bank bank = plugin.getBankRepository().getByIdentifier(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[2]);
        if (playerToUntrust == null) {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRUST_OTHER))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
                plugin.debugf("%s does not have permission to untrust a player from bank %s as a co-owner",
                        sender.getName(), bank.getName());
                sender.sendMessage(Message.MUST_BE_OWNER.translate());
                return true;
            }
            plugin.debugf("%s does not have permission to untrust a player from bank %s", sender.getName(), bank.getName());
            sender.sendMessage(Message.NO_PERMISSION_BANK_UNTRUST_OTHER.translate());
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRUST_ADMIN)) {
            plugin.debugf("%s does not have permission to untrust a player from admin bank %s", sender.getName(), bank.getName());
            sender.sendMessage(Message.NO_PERMISSION_BANK_UNTRUST_ADMIN.translate());
            return true;
        }

        if (!bank.isTrusted(playerToUntrust)) {
            plugin.debugf("%s was not trusted at bank #%d", playerToUntrust.getName(), bank.getID());
            sender.sendMessage(Message.NOT_A_COOWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
            return true;
        }

        plugin.debugf("%s has untrusted %s from bank #%d",
                sender.getName(), playerToUntrust.getName(), bank.getID());
        sender.sendMessage(Message.REMOVED_COOWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
        bank.untrustPlayer(playerToUntrust);
        plugin.getDatabase().removeCoOwner(bank, playerToUntrust, null);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .filter(bank -> bank.isOwner(player)
                            || (bank.isPlayerBank() && player.hasPermission(Permissions.BANK_TRUST_OTHER))
                            || (bank.isAdminBank() && player.hasPermission(Permissions.BANK_TRUST_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 2) {
            Bank bank = plugin.getBankRepository().getByIdentifier(args[1]);
            if (bank == null)
                return Collections.emptyList();
            List<String> coowners = bank.getCoOwners().stream().map(OfflinePlayer::getName).collect(Collectors.toList());
            return Utils.filter(coowners, name -> Utils.startsWithIgnoreCase(name, args[1]));
        }
        return Collections.emptyList();
    }

}
