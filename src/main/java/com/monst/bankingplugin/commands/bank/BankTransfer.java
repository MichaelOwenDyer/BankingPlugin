package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankTransferEvent;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTransfer extends SubCommand.BankSubCommand implements ConfirmableSubCommand {

    BankTransfer() {
        super("transfer", false);
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
        PLUGIN.debug(sender.getName() + " wants to transfer bank ownership");

        if (!sender.hasPermission(Permissions.BANK_TRANSFER)) {
            PLUGIN.debug(sender.getName() + " does not have permission to transfer bank ownership");
            sender.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_TRANSFER));
            return true;
        }

        if (args.length < 2)
            return false;

        Bank bank = bankRepo.getByIdentifier(args[1]);
        if (bank == null) {
            PLUGIN.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(Messages.get(Message.BANK_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
            return true;
        }
        OfflinePlayer newOwner = null;
        if (args.length > 2) {
            newOwner = Utils.getPlayer(args[2]);
            if (newOwner == null) {
                sender.sendMessage(Messages.get(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
                return true;
            }
        }

        if (newOwner != null && bank.isOwner(newOwner)) {
            PLUGIN.debug(newOwner.getName() + " is already owner of that bank");
            sender.sendMessage(Messages.get(Message.ALREADY_OWNER,
                    new Replacement(Placeholder.PLAYER, newOwner::getName)
            ));
            return true;
        }
        if (newOwner == null && bank.isAdminBank()) {
            PLUGIN.debug("Bank is already an admin bank");
            sender.sendMessage(Messages.get(Message.BANK_ALREADY_ADMIN, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRANSFER_ADMIN)) {
            PLUGIN.debug(sender.getName() + " does not have permission to transfer an admin bank");
            sender.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_TRANSFER_ADMIN));
            return true;
        }
        if (newOwner == null && !sender.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            PLUGIN.debug(sender.getName() + " does not have permission to transfer a bank to the admins");
            sender.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_CREATE_ADMIN));
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRANSFER_OTHER))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender)) {
                PLUGIN.debug(sender.getName() + " does not have permission to transfer ownership as a co-owner");
                sender.sendMessage(Messages.get(Message.MUST_BE_OWNER));
                return true;
            }
            PLUGIN.debug(sender.getName() + " does not have permission to transfer ownership of another player's bank");
            sender.sendMessage(Messages.get(Message.NO_PERMISSION_BANK_TRANSFER_OTHER));
            return true;
        }

        if (sender instanceof Player && Config.confirmOnTransfer.get() && isConfirmed((Player) sender, args)) {
            sender.sendMessage(Messages.get(Message.BANK_CONFIRM_TRANSFER,
                    new Replacement(Placeholder.PLAYER, newOwner != null ? newOwner.getName() : "ADMIN"),
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
            ));
            sender.sendMessage(Messages.get(Message.EXECUTE_AGAIN_TO_CONFIRM));
            return true;
        }

        BankTransferEvent event = new BankTransferEvent(sender, bank, newOwner);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Bank transfer event cancelled");
            return true;
        }

        MailingRoom mailingRoom = new MailingRoom(Messages.get(Message.BANK_TRANSFERRED,
                new Replacement(Placeholder.PLAYER, newOwner != null ? newOwner.getName() : "ADMIN"),
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
        ));
        mailingRoom.addRecipient(sender);
        mailingRoom.send();
        mailingRoom.newMessage(Messages.get(Message.BANK_TRANSFERRED_TO_YOU,
                new Replacement(Placeholder.PLAYER, sender::getName),
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
        ));
        mailingRoom.addOfflineRecipient(newOwner);
        mailingRoom.removeRecipient(sender);
        mailingRoom.send();

        bank.setOwner(newOwner);
        bankRepo.update(bank, BankField.OWNER);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 1) {
            return bankRepo.getAll().stream()
                    .filter(bank -> bank.isOwner(p)
                            || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRANSFER_OTHER))
                            || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> onlinePlayers = Utils.getOnlinePlayerNames();
            if (!p.hasPermission(Permissions.BANK_TRANSFER_OTHER) && !p.hasPermission(Permissions.BANK_TRANSFER_ADMIN))
                onlinePlayers.remove(p.getName());
            Bank bank = bankRepo.getByIdentifier(args[0]);
            if (bank != null && bank.isPlayerBank())
                onlinePlayers.remove(bank.getOwner().getName());
            return Utils.filter(onlinePlayers, name -> Utils.startsWithIgnoreCase(name, args[1]));
        }
        return Collections.emptyList();
    }

}
