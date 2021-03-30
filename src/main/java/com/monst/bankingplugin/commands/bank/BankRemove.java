package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRemove extends BankCommand.SubCommand implements ConfirmableSubCommand {

    BankRemove() {
        super("remove", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_REMOVE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        plugin.debug(sender.getName() + " wants to remove a bank");

        Bank bank = getBank(sender, args);
        if (bank == null)
            return true;

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || sender.hasPermission(Permissions.BANK_REMOVE_OTHER))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender))) {
                plugin.debug(sender.getName() + " does not have permission to remove another player's bank as a co-owner");
                sender.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to remove another player's bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_REMOVE_OTHER));
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to remove an admin bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_REMOVE_ADMIN));
            return true;
        }

        if (sender instanceof Player) {
            Player executor = (Player) sender;
            if (Config.confirmOnRemove.get() && !isConfirmed(executor, args)) {
                sender.sendMessage(LangUtils.getMessage(Message.BANK_CONFIRM_REMOVE,
                        new Replacement(Placeholder.NUMBER_OF_BANKS, 1),
                        new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, bank.getAccounts().size())
                ));
                return true;
            }
            if (bank.isPlayerBank() && Config.reimburseBankCreation.get() && bank.isOwner(executor)) {
                double reimbursement = Config.bankCreationPrice.get();
                if (reimbursement > 0) {
                    if (PayrollOffice.deposit(executor, reimbursement))
                        executor.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                                new Replacement(Placeholder.AMOUNT, reimbursement)
                        ));
                }
            }
        }

        BankRemoveEvent event = new BankRemoveEvent(sender, bank);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank remove event cancelled");
            return true;
        }

        int accountsRemoved = bank.getAccounts().size();
        bankRepo.remove(bank, true);
        plugin.debugf("Bank #%d and %d accounts removed from the database.", bank.getID(), accountsRemoved);
        MailingRoom mailingRoom = new MailingRoom(LangUtils.getMessage(Message.BANK_REMOVED,
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accountsRemoved)
        ));
        mailingRoom.addOfflineRecipient(bank.getTrustedPlayers());
        mailingRoom.addOfflineRecipient(bank.getCustomers());
        mailingRoom.addRecipient(sender);
        mailingRoom.send();
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return bankRepo.getAll().stream()
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
