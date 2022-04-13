package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTrust extends SubCommand {

    BankTrust(BankingPlugin plugin) {
		super(plugin, "trust");
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
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_TRUST;
    }

    @Override
    protected int getMinimumArguments() {
        return 2;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws ExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        OfflinePlayer playerToTrust = Utils.getPlayer(args[1]);
        if (playerToTrust == null)
            throw new ExecutionException(plugin, Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender)) || Permission.BANK_TRUST_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_TRUST_OTHER);
        }

        if (bank.isAdminBank() && Permission.BANK_TRUST_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_TRUST_ADMIN);

        if (bank.isTrusted(playerToTrust)) {
            if (bank.isOwner(playerToTrust))
                throw new ExecutionException(plugin, Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));
            throw new ExecutionException(plugin, Message.ALREADY_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));
        }

        plugin.debugf("%s has trusted %s to bank #%d", sender.getName(), playerToTrust.getName(), bank.getID());
        sender.sendMessage(Message.ADDED_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate(plugin));
        bank.trustPlayer(playerToTrust);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findByPlayerAllowedToModify(player, Permission.BANK_TRUST_OTHER, Permission.BANK_TRUST_ADMIN, true)
                    .stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        if (args.length == 2 && plugin.getBankService().findByName(args[0]) != null)
            return Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
