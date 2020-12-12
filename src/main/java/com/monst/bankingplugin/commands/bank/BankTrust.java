package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTrust extends BankCommand.SubCommand {

    BankTrust() {
        super("trust", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_TRUST;
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

        if (!sender.hasPermission(Permissions.BANK_TRUST)) {
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRUST));
            return true;
        }
        Bank bank = plugin.getBankUtils().get(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }
        OfflinePlayer playerToTrust = Utils.getPlayer(args[2]);
        if (playerToTrust == null) {
            sender.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRUST_OTHER))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
                plugin.debugf("%s does not have permission to trust a player to bank %s as a co-owner", sender.getName(), bank.getName());
                sender.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return true;
            }
            plugin.debugf("%s does not have permission to trust a player to bank %s", sender.getName(), bank.getName());
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRUST_OTHER));
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRUST_ADMIN)) {
            plugin.debugf("%s does not have permission to trust a player to admin bank %s", sender.getName(), bank.getName());
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRUST_ADMIN));
            return true;
        }

        if (bank.isTrusted(playerToTrust)) {
            plugin.debugf("%s was already trusted at bank %s (#%d)", playerToTrust.getName(), bank.getName(), bank.getID());
            sender.sendMessage(LangUtils.getMessage(bank.isOwner(playerToTrust) ? Message.ALREADY_OWNER : Message.ALREADY_COOWNER,
                    new Replacement(Placeholder.PLAYER, playerToTrust::getName)
            ));
            return true;
        }

        plugin.debugf("%s has trusted %s to bank %s (#%d)",
                sender.getName(), playerToTrust.getName(), bank.getName(), bank.getID());
        sender.sendMessage(LangUtils.getMessage(Message.ADDED_COOWNER,
                new Replacement(Placeholder.PLAYER, playerToTrust::getName)
        ));
        bank.trustPlayer(playerToTrust);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankRepo.get().stream()
                    .filter(bank -> bank.isOwner(p)
                            || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRUST_OTHER))
                            || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRUST_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
            if (!p.hasPermission(Permissions.BANK_TRUST_OTHER) && !p.hasPermission(Permissions.BANK_TRUST_ADMIN))
                onlinePlayers.remove(p.getName());
            return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[2].toLowerCase()));
        }
        return Collections.emptyList();
    }

}
