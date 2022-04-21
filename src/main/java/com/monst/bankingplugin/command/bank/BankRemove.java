package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankRemoveEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.external.BankVisualization;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BankRemove extends SubCommand {

    BankRemove(BankingPlugin plugin) {
        super(plugin, "remove");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_REMOVE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_REMOVE;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        Bank bank;
        if (args.length == 0 && sender instanceof Player) {
            bank = plugin.getBankService().findContaining((Player) sender);
            if (bank == null)
                throw new ExecutionException(plugin, Message.MUST_STAND_IN_OR_SPECIFY_BANK);
        } else {
            bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));
        }

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isOwner((Player) sender))
                || Permission.BANK_REMOVE_OTHER.ownedBy(sender))) {
            if (sender instanceof Player && bank.isTrusted(((Player) sender)))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_REMOVE_OTHER);
        }

        if (bank.isAdminBank() && Permission.BANK_REMOVE_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_REMOVE_ADMIN);

        if (sender instanceof Player) {
            Player executor = (Player) sender;
            if (plugin.config().confirmOnRemove.get() && isFirstUsage(executor, Objects.hash("remove", bank))) {
                sender.sendMessage(Message.BANK_ABOUT_TO_REMOVE
                        .with(Placeholder.BANK_NAME).as(bank.getName())
                        .and(Placeholder.NUMBER_OF_ACCOUNTS).as(bank.getNumberOfAccounts())
                        .translate(plugin));
                return;
            }
            if (bank.isOwner(executor) && plugin.config().reimburseBankCreation.get()) {
                final double finalReimbursement = plugin.config().bankCreationPrice.get().doubleValue();
                if (finalReimbursement > 0) {
                    if (plugin.getPaymentService().deposit(executor, finalReimbursement))
                        executor.sendMessage(Message.ACCOUNT_REIMBURSEMENT_RECEIVED
                                .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                                .translate(plugin));
                }
            }
        }

        new BankRemoveEvent(sender, bank).fire();

        int accountsRemoved = bank.getNumberOfAccounts();
        plugin.getBankService().remove(bank);
        plugin.getSchedulerService().scheduleAll();
        if (sender instanceof Player && plugin.isGriefPreventionIntegrated())
            BankVisualization.revert((Player) sender);
        plugin.debugf("Bank #%d and %d accounts removed from the database.", bank.getID(), accountsRemoved);
        String message = Message.BANK_REMOVED
                .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .and(Placeholder.NUMBER_OF_ACCOUNTS).as(accountsRemoved)
                .translate(plugin);
        bank.getMailingList(sender).forEach(player -> player.sendMessage(message));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return plugin.getBankService()
                .findByPlayerAllowedToModify(player, Permission.BANK_REMOVE_OTHER, Permission.BANK_REMOVE_ADMIN, true)
                .stream()
                .map(Bank::getName)
                .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

}
