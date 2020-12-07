package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankTransferEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Messenger;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankTransfer extends BankCommand.SubCommand implements ConfirmableSubCommand {

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
        plugin.debug(sender.getName() + " wants to transfer bank ownership");

        if (!sender.hasPermission(Permissions.BANK_TRANSFER)) {
            plugin.debug(sender.getName() + " does not have permission to transfer bank ownership");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRANSFER));
            return true;
        }

        if (args.length < 2)
            return false;

        Bank bank = bankUtils.getBank(args[1]);
        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }
        OfflinePlayer newOwner = null;
        if (args.length > 2) {
            newOwner = Utils.getPlayer(args[2]);
            if (newOwner == null) {
                sender.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
                return true;
            }
        }

        if (newOwner != null && bank.isOwner(newOwner)) {
            plugin.debug(newOwner.getName() + " is already owner of that bank");
            sender.sendMessage(LangUtils.getMessage(Message.ALREADY_OWNER,
                    new Replacement(Placeholder.PLAYER, newOwner::getName)
            ));
            return true;
        }
        if (newOwner == null && bank.isAdminBank()) {
            plugin.debug("Bank is already an admin bank");
            sender.sendMessage(LangUtils.getMessage(Message.BANK_ALREADY_ADMIN, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRANSFER_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer an admin bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRANSFER_ADMIN));
            return true;
        }
        if (newOwner == null && !sender.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer a bank to the admins");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_CREATE_ADMIN));
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRANSFER_OTHER))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender)) {
                plugin.debug(sender.getName() + " does not have permission to transfer ownership as a co-owner");
                sender.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to transfer ownership of another player's bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_TRANSFER_OTHER));
            return true;
        }

        if (sender instanceof Player && Config.confirmOnTransfer && isConfirmed((Player) sender, args)) {
            sender.sendMessage(LangUtils.getMessage(Message.BANK_CONFIRM_TRANSFER,
                    new Replacement(Placeholder.PLAYER, newOwner != null ? newOwner.getName() : "ADMIN"),
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
            ));
            sender.sendMessage(LangUtils.getMessage(Message.EXECUTE_AGAIN_TO_CONFIRM));
            return true;
        }

        BankTransferEvent event = new BankTransferEvent(sender, bank, newOwner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank transfer event cancelled");
            return true;
        }

        Messenger messenger = new Messenger(LangUtils.getMessage(Message.BANK_TRANSFERRED,
                new Replacement(Placeholder.PLAYER, newOwner != null ? newOwner.getName() : "ADMIN"),
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
        ));
        messenger.addRecipient(sender);
        messenger.send();
        messenger.setMessage(LangUtils.getMessage(Message.BANK_TRANSFERRED_TO_YOU,
                new Replacement(Placeholder.PLAYER, sender::getName),
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
        ));
        messenger.addOfflineRecipient(newOwner);
        messenger.removeRecipient(sender);
        messenger.send();

        bank.transferOwnership(newOwner);
        bankUtils.addBank(bank, true);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankUtils.getBanks().stream()
                    .filter(bank -> bank.isOwner(p)
                            || (bank.isPlayerBank() && p.hasPermission(Permissions.BANK_TRANSFER_OTHER))
                            || (bank.isAdminBank() && p.hasPermission(Permissions.BANK_TRANSFER_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
            if (!p.hasPermission(Permissions.BANK_TRANSFER_OTHER) && !p.hasPermission(Permissions.BANK_TRANSFER_ADMIN))
                onlinePlayers.remove(p.getName());
            Bank bank = bankUtils.getBank(args[1]);
            if (bank != null && bank.isPlayerBank())
                onlinePlayers.remove(bank.getOwner().getName());
            return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[2].toLowerCase()));
        }
        return Collections.emptyList();
    }

}
