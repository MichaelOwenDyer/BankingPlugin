package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRemove extends BankSubCommand implements ConfirmableCommand {

    public BankRemove() {
        super("remove", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_REMOVE : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        plugin.debug(sender.getName() + " wants to remove a bank");

        Bank bank = getBank(sender, args);
        if (bank == null)
            return true;

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_REMOVE_OTHER))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
                plugin.debug(sender.getName() + " does not have permission to remove another player's bank as a co-owner");
                sender.sendMessage(Messages.MUST_BE_OWNER);
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to remove another player's bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVE_OTHER);
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to remove an admin bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVE_ADMIN);
            return true;
        }

        if (sender instanceof Player && Config.confirmOnRemove && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, 1, "", bank.getAccounts().size(),
                    bank.getAccounts().size() == 1 ? "" : "s"));
            sender.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
            return true;
        }

        BankRemoveEvent event = new BankRemoveEvent(sender, bank);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank remove event cancelled");
            return true;
        }

        if (sender instanceof Player) {
            double creationPrice = bank.isAdminBank() ? Config.bankCreationPriceAdmin : Config.bankCreationPricePlayer;
            boolean reimburse = bank.isAdminBank() ? Config.reimburseBankCreationAdmin : Config.reimburseBankCreationPlayer;
            creationPrice *= reimburse ? 1 : 0;

            Player executor = (Player) sender;
            if (creationPrice > 0 && (bank.isAdminBank() || bank.isOwner(executor))) {
                double finalCreationPrice = creationPrice;
                Utils.depositPlayer(executor.getPlayer(), bank.getSelection().getWorld().getName(), finalCreationPrice, Callback.of(plugin,
                        result -> executor.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED, Utils.format(finalCreationPrice))),
                        throwable -> executor.sendMessage(Messages.ERROR_OCCURRED))
                );
            }
        }

        bankUtils.removeBank(bank, true);
        plugin.debug("Bank #" + bank.getID() + " removed from the database");
        sender.sendMessage(Messages.BANK_REMOVED);
        Utils.notifyPlayers(String.format(Messages.PLAYER_REMOVED_BANK, sender.getName(), bank.getName()),
                Utils.mergeCollections(bank.getTrustedPlayers(), bank.getCustomers()), sender
        );
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return bankUtils.getBanksCopy().stream()
                    .filter(bank -> (sender instanceof Player && bank.isOwner((Player) sender))
                            || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_REMOVE_OTHER))
                            || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
