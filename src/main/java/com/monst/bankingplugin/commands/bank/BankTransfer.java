package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.commands.PlayerCache;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankTransferEvent;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Pair;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTransfer extends SubCommand.BankSubCommand {

    BankTransfer(BankingPlugin plugin) {
        super(plugin, "transfer", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_TRANSFER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_TRANSFER;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to transfer bank ownership");

        if (!sender.hasPermission(Permissions.BANK_TRANSFER)) {
            plugin.debug(sender.getName() + " does not have permission to transfer bank ownership");
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRANSFER.translate());
            return true;
        }

        if (args.length < 2)
            return false;

        Bank bank = plugin.getBankRepository().getByIdentifier(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }
        OfflinePlayer newOwner = null;
        if (args.length > 2) {
            newOwner = Utils.getPlayer(args[2]);
            if (newOwner == null) {
                sender.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[2]).translate());
                return true;
            }
        }

        if (newOwner != null && bank.isOwner(newOwner)) {
            plugin.debug(newOwner.getName() + " is already owner of that bank");
            sender.sendMessage(Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
            return true;
        }
        if (newOwner == null && bank.isAdminBank()) {
            plugin.debug("Bank is already an admin bank");
            sender.sendMessage(Message.BANK_ALREADY_ADMIN.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRANSFER_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer an admin bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRANSFER_ADMIN.translate());
            return true;
        }
        if (newOwner == null && !sender.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer a bank to the admins");
            sender.sendMessage(Message.NO_PERMISSION_BANK_CREATE_ADMIN.translate());
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRANSFER_OTHER))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender)) {
                plugin.debug(sender.getName() + " does not have permission to transfer ownership as a co-owner");
                sender.sendMessage(Message.MUST_BE_OWNER.translate());
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to transfer ownership of another player's bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_TRANSFER_OTHER.translate());
            return true;
        }

        if (sender instanceof Player && Config.confirmOnTransfer.get()
                && !PlayerCache.put((Player) sender, new BankTransferPair(bank, newOwner))) {
            sender.sendMessage(Message.BANK_CONFIRM_TRANSFER
                    .with(Placeholder.PLAYER).as(newOwner != null ? newOwner.getName() : "ADMIN")
                    .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .translate());
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate());
            return true;
        }

        BankTransferEvent event = new BankTransferEvent(sender, bank, newOwner);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Bank transfer event cancelled");
            return true;
        }

        bank.setOwner(newOwner);
        String message = Message.BANK_TRANSFERRED
                .with(Placeholder.PLAYER).as(bank.getOwnerDisplayName())
                .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .translate();
        MailingRoom.draft(message).to(sender).send();
        message = Message.BANK_TRANSFERRED_TO_YOU
                .with(Placeholder.PLAYER).as(sender.getName())
                .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .translate();
        MailingRoom.draft(message).to(newOwner).butNotTo(sender).send();

        plugin.getBankRepository().update(bank, BankField.OWNER);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .filter(bank -> bank.isOwner(player)
                            || (bank.isPlayerBank() && player.hasPermission(Permissions.BANK_TRANSFER_OTHER))
                            || (bank.isAdminBank() && player.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 2) {
            List<String> onlinePlayers = Utils.getOnlinePlayerNames();
            if (!player.hasPermission(Permissions.BANK_TRANSFER_OTHER) && !player.hasPermission(Permissions.BANK_TRANSFER_ADMIN))
                onlinePlayers.remove(player.getName());
            Bank bank = plugin.getBankRepository().getByIdentifier(args[0]);
            if (bank != null && bank.isPlayerBank())
                onlinePlayers.remove(bank.getOwnerName());
            return Utils.filter(onlinePlayers, name -> Utils.startsWithIgnoreCase(name, args[1]));
        }
        return Collections.emptyList();
    }

    private static class BankTransferPair extends Pair<Bank, OfflinePlayer> {
        public BankTransferPair(Bank bank, OfflinePlayer newOwner) {
            super(bank, newOwner);
        }
    }

}
