package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTrust extends SubCommand.BankSubCommand {

    BankTrust(BankingPlugin plugin) {
		super(plugin, "trust", false);
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_TRUST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return false;

        plugin.debug(sender.getName() + " wants to trust a player to a bank");

        if (Permission.BANK_TRUST.notOwnedBy(sender)) {
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRUST.translate());
            return true;
        }
        Bank bank = plugin.getBankRepository().getByIdentifier(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }
        OfflinePlayer playerToTrust = Utils.getPlayer(args[2]);
        if (playerToTrust == null) {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || Permission.BANK_TRUST_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender)) {
                plugin.debugf("%s does not have permission to trust a player to bank #%d as a co-owner", sender.getName(), bank.getID());
                sender.sendMessage(Message.MUST_BE_OWNER.translate());
                return true;
            }
            plugin.debugf("%s does not have permission to trust a player to bank #%d", sender.getName(), bank.getID());
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRUST_OTHER.translate());
            return true;
        }

        if (bank.isAdminBank() && Permission.BANK_TRUST_ADMIN.notOwnedBy(sender)) {
            plugin.debugf("%s does not have permission to trust a player to admin bank #%d", sender.getName(), bank.getID());
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRUST_ADMIN.translate());
            return true;
        }

        if (bank.isTrusted(playerToTrust)) {
            plugin.debugf("%s was already trusted at bank #%d", playerToTrust.getName(), bank.getID());
            Message message = bank.isOwner(playerToTrust) ? Message.ALREADY_OWNER : Message.ALREADY_COOWNER;
            sender.sendMessage(message.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate());
            return true;
        }

        plugin.debugf("%s has trusted %s to bank #%d", sender.getName(), playerToTrust.getName(), bank.getID());
        sender.sendMessage(Message.ADDED_COOWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate());
        bank.trustPlayer(playerToTrust);
        plugin.getDatabase().addCoOwner(bank, playerToTrust, null);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .filter(bank -> bank.isOwner(player)
                            || (bank.isPlayerBank() && Permission.BANK_TRUST_OTHER.ownedBy(player))
                            || (bank.isAdminBank() && Permission.BANK_TRUST_ADMIN.ownedBy(player)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 2) {
            List<String> onlinePlayers = Utils.getOnlinePlayerNames();
            if (Permission.BANK_TRUST_OTHER.notOwnedBy(player) && Permission.BANK_TRUST_ADMIN.notOwnedBy(player))
                onlinePlayers.remove(player.getName());
            return Utils.filter(onlinePlayers, name -> Utils.startsWithIgnoreCase(name, args[1]));
        }
        return Collections.emptyList();
    }

}
