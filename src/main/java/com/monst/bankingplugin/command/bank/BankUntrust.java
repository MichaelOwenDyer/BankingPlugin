package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankUntrust extends SubCommand {

    BankUntrust(BankingPlugin plugin) {
		super(plugin, "untrust");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_TRUST;
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
    protected void execute(CommandSender sender, String[] args) throws ExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null)
            throw new ExecutionException(plugin, Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender)) || Permission.BANK_TRUST_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender)))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_UNTRUST_OTHER);
        }

        if (bank.isAdminBank() && Permission.BANK_TRUST_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_UNTRUST_ADMIN);

        if (!bank.isTrusted(playerToUntrust))
            throw new ExecutionException(plugin, Message.NOT_A_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()));

        plugin.debugf("%s has untrusted %s from bank #%d", sender.getName(), playerToUntrust.getName(), bank.getID());
        sender.sendMessage(Message.REMOVED_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate(plugin));
        bank.untrustPlayer(playerToUntrust);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return plugin.getBankService()
                    .findByPlayerAllowedToModify(player, Permission.BANK_TRUST_OTHER, Permission.BANK_TRUST_ADMIN, true)
                    .stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            Bank bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                return Collections.emptyList();
            List<String> co_owners = new ArrayList<>();
            for (OfflinePlayer co_owner : bank.getCoOwners()) {
                if (co_owner.getName() != null && Utils.startsWithIgnoreCase(co_owner.getName(), args[1]))
                    co_owners.add(co_owner.getName());
            }
            return co_owners;
        }
        return Collections.emptyList();
    }

}
