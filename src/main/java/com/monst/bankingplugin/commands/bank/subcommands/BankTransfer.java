package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankTransferEvent;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankTransfer extends BankSubCommand implements ConfirmableSubCommand {

    public BankTransfer() {
        super("transfer", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.BANK_TRANSFER) ? Messages.COMMAND_USAGE_BANK_TRANSFER : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to transfer bank ownership");

        if (!sender.hasPermission(Permissions.BANK_TRANSFER)) {
            plugin.debug(sender.getName() + " does not have permission to transfer bank ownership");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER);
            return true;
        }

        if (args.length < 2)
            return false;

        Bank bank = bankUtils.lookupBank(args[1]);
        if (bank == null) {
            plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
            sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
            return true;
        }
        OfflinePlayer newOwner = null;
        if (args.length > 2) {
            newOwner = Utils.getPlayer(args[2]);
            if (newOwner == null) {
                sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[2]));
                return true;
            }
        }

        if (newOwner != null && bank.isOwner(newOwner)) {
            boolean isExecutor = sender instanceof Player && Utils.samePlayer((Player) sender, newOwner);
            plugin.debug(newOwner.getName() + " is already owner of that bank");
            sender.sendMessage(String.format(Messages.ALREADY_OWNER, isExecutor ? "You are" : newOwner.getName() + " is", "bank"));
            return true;
        }
        if (newOwner == null && bank.isAdminBank()) {
            plugin.debug("Bank is already an admin bank");
            sender.sendMessage(Messages.ALREADY_ADMIN_BANK);
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_TRANSFER_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer an admin bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_ADMIN);
            return true;
        }
        if (newOwner == null && !sender.hasPermission(Permissions.BANK_CREATE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to transfer a bank to the admins");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_TO_ADMIN);
            return true;
        }
        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_TRANSFER_OTHER))) {
            if (sender instanceof Player && bank.isTrusted((Player) sender)) {
                plugin.debug(sender.getName() + " does not have permission to transfer ownership as a co-owner");
                sender.sendMessage(Messages.MUST_BE_OWNER);
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to transfer ownership of another player's bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_TRANSFER_OTHER);
            return true;
        }

        if (sender instanceof Player && Config.confirmOnTransfer && isConfirmed((Player) sender, args)) {
            sender.sendMessage(String.format(Messages.ABOUT_TO_TRANSFER,
                    bank.getName(),
                    newOwner != null ? newOwner.getName() : "ADMIN"));
            sender.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
            return true;
        }

        BankTransferEvent event = new BankTransferEvent(sender, bank, newOwner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank transfer event cancelled");
            return true;
        }

        boolean isSelf = sender instanceof Player && Utils.samePlayer(newOwner, ((Player) sender));
        sender.sendMessage(String.format(Messages.OWNERSHIP_TRANSFERRED, "You", isSelf ? "yourself"
                : (newOwner != null ? newOwner.getName() : "ADMIN"), "bank " + bank.getColorizedName()));

        if (!isSelf)
            Utils.notifyPlayers(
                    String.format(Messages.OWNERSHIP_TRANSFERRED, sender.getName(), "you", "bank " + bank.getColorizedName()),
                    newOwner
            );

        Set<OfflinePlayer> toNotify = Utils.mergeCollections(bank.getCustomers(), bank.getTrustedPlayers());
        toNotify.remove(newOwner);
        if (sender instanceof Player)
            toNotify.remove(sender);
        Utils.notifyPlayers(
                String.format(Messages.OWNERSHIP_TRANSFERRED, sender.getName(), newOwner != null ? newOwner.getName() : "ADMIN", "bank " + bank.getColorizedName()),
                toNotify
        );

        bank.transferOwnership(newOwner);
        bankUtils.addBank(bank, true);
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length == 2) {
            return bankUtils.getBanksCopy().stream()
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
            return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[2].toLowerCase()));
        }
        return Collections.emptyList();
    }

}
