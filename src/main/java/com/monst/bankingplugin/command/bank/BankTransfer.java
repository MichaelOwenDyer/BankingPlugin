package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.CommandCache;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankTransferEvent;
import com.monst.bankingplugin.exception.CancelledException;
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
import java.util.Objects;
import java.util.stream.Collectors;

public class BankTransfer extends SubCommand {

    BankTransfer(BankingPlugin plugin) {
        super(plugin, "transfer");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_TRANSFER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_TRANSFER;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_TRANSFER;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        OfflinePlayer newOwner = null;
        if (args.length > 1) {
            newOwner = Utils.getPlayer(args[1]);
            if (newOwner == null)
                throw new ExecutionException(plugin, Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]));
        }

        if (newOwner != null && bank.isOwner(newOwner))
            throw new ExecutionException(plugin, Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(newOwner.getName()));

        if (newOwner == null && bank.isAdminBank())
            throw new ExecutionException(plugin, Message.BANK_ALREADY_ADMIN.with(Placeholder.BANK_NAME).as(bank.getColorizedName()));

        if (bank.isAdminBank() && Permission.BANK_TRANSFER_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_TRANSFER_ADMIN);

        if (newOwner == null && Permission.BANK_CREATE_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_CREATE_ADMIN);

        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
                || Permission.BANK_TRANSFER_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_TRANSFER_OTHER);
        }

        if (sender instanceof Player && plugin.config().confirmOnTransfer.get()
                && CommandCache.isFirstUsage((Player) sender, Objects.hash("transfer", bank, newOwner))) {
            sender.sendMessage(Message.ABOUT_TO_TRANSFER_BANK
                    .with(Placeholder.PLAYER).as(newOwner != null ? newOwner.getName() : "ADMIN")
                    .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .translate(plugin));
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate(plugin));
            return;
        }

        new BankTransferEvent(sender, bank, newOwner).fire();

        if (bank.isPlayerBank() && plugin.config().trustOnTransfer.get())
            bank.trustPlayer(bank.getOwner());
        bank.setOwner(newOwner);
        plugin.getBankService().update(bank);
        if (newOwner == null)
            sender.sendMessage(Message.BANK_TRANSFERRED_TO_ADMIN
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .translate(plugin));
        else {
            sender.sendMessage(Message.BANK_TRANSFERRED
                    .with(Placeholder.PLAYER).as(newOwner.getName())
                    .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .translate(plugin));
            if (!(sender instanceof Player && ((Player) sender).getUniqueId().equals(newOwner.getUniqueId())))
                Utils.message(newOwner, Message.BANK_TRANSFERRED_TO_YOU
                        .with(Placeholder.PLAYER).as(sender.getName())
                        .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                        .translate(plugin));
        }
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findByPlayerAllowedToModify(player, Permission.BANK_TRANSFER_OTHER, Permission.BANK_TRANSFER_ADMIN, true)
                    .stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        if (args.length == 2)
            return Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
