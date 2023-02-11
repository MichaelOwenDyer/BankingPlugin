package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTrust extends SubCommand {

    BankTrust(BankingPlugin plugin) {
		super(plugin, "trust");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_TRUST;
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
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        OfflinePlayer playerToTrust = getPlayer(args[1]);
        if (playerToTrust == null)
            throw err(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender)) || Permissions.BANK_TRUST_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender))
                throw err(Message.MUST_BE_OWNER);
            throw err(Message.NO_PERMISSION_BANK_TRUST_OTHER);
        }

        if (bank.isAdminBank() && Permissions.BANK_TRUST_ADMIN.notOwnedBy(sender))
            throw err(Message.NO_PERMISSION_BANK_TRUST_ADMIN);

        if (bank.isTrusted(playerToTrust)) {
            if (bank.isOwner(playerToTrust))
                throw err(Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));
            throw err(Message.ALREADY_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));
        }

        plugin.debug("%s has trusted %s to bank #%d", sender.getName(), playerToTrust.getName(), bank.getID());
        sender.sendMessage(Message.ADDED_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate(plugin));
        bank.trustPlayer(playerToTrust);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findNamesByPlayerAllowedToModify(player,
                            Permissions.BANK_TRUST_OTHER.ownedBy(player),
                            Permissions.BANK_TRUST_ADMIN.ownedBy(player), true)
                    .stream()
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        if (args.length == 2 && plugin.getBankService().findByName(args[0]) != null)
            return Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
