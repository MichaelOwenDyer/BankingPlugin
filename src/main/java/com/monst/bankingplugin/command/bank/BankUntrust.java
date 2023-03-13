package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankUntrust extends SubCommand {

    BankUntrust(BankingPlugin plugin) {
		super(plugin, "untrust");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_UNTRUST;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_UNTRUST;
    }

    @Override
    protected int getMinimumArguments() {
        return 2;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        OfflinePlayer playerToUntrust = getPlayer(args[1]);
        if (playerToUntrust == null)
            throw err(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender)) || Permissions.BANK_TRUST_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender)))
                throw err(Message.MUST_BE_OWNER);
            throw err(Message.NO_PERMISSION_BANK_UNTRUST_OTHER);
        }

        if (bank.isAdminBank() && Permissions.BANK_TRUST_ADMIN.notOwnedBy(sender))
            throw err(Message.NO_PERMISSION_BANK_UNTRUST_ADMIN);

        if (!bank.isTrusted(playerToUntrust))
            throw err(Message.NOT_A_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()));
    
        bank.untrustPlayer(playerToUntrust);
        sender.sendMessage(Message.REMOVED_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate(plugin));
        plugin.debug("%s has untrusted %s from bank %s", sender.getName(), playerToUntrust.getName(), bank);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return plugin.getBankService()
                    .findNamesByPlayerAllowedToModify(player,
                            Permissions.BANK_TRUST_OTHER.ownedBy(player),
                            Permissions.BANK_TRUST_ADMIN.ownedBy(player), true)
                    .stream()
                    .filter(name -> containsIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            Bank bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                return Collections.emptyList();
            return bank.getCoOwners().stream()
                    .map(OfflinePlayer::getName)
                    .filter(name -> containsIgnoreCase(name, args[1]))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
