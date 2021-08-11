package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.PlayerCache;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRemove extends SubCommand.BankSubCommand {

    BankRemove(BankingPlugin plugin) {
        super(plugin, "remove", false);
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
                sender.sendMessage(Message.MUST_BE_OWNER.translate());
                return true;
            }
            plugin.debug(sender.getName() + " does not have permission to remove another player's bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_REMOVE_OTHER.translate());
            return true;
        }

        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_REMOVE_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to remove an admin bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_REMOVE_ADMIN.translate());
            return true;
        }

        if (sender instanceof Player) {
            Player executor = (Player) sender;
            if (Config.confirmOnRemove.get() && !PlayerCache.put(executor, bank)) {
                sender.sendMessage(Message.BANK_CONFIRM_REMOVE
                        .with(Placeholder.NUMBER_OF_BANKS).as(1)
                        .and(Placeholder.NUMBER_OF_ACCOUNTS).as(bank.getAccounts().size())
                        .translate());
                return true;
            }
            if (bank.isPlayerBank() && Config.reimburseBankCreation.get() && bank.isOwner(executor)) {
                BigDecimal reimbursement = Config.bankCreationPrice.get();
                if (reimbursement.signum() > 0) {
                    if (PayrollOffice.deposit(executor, reimbursement))
                        executor.sendMessage(Message.REIMBURSEMENT_RECEIVED.with(Placeholder.AMOUNT).as(reimbursement).translate());
                }
            }
        }

        BankRemoveEvent event = new BankRemoveEvent(sender, bank);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Bank remove event cancelled");
            return true;
        }

        int accountsRemoved = bank.getAccounts().size();
        plugin.getBankRepository().remove(bank, true);
        plugin.debugf("Bank #%d and %d accounts removed from the database.", bank.getID(), accountsRemoved);
        String message = Message.BANK_REMOVED
                .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .and(Placeholder.NUMBER_OF_ACCOUNTS).as(accountsRemoved)
                .translate();
        MailingRoom.draft(message).to(bank.getTrustedPlayers()).and(bank.getCustomers()).and(sender).send();
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return plugin.getBankRepository().getAll().stream()
                    .filter(bank -> bank.isOwner(player)
                            || (bank.isPlayerBank() && player.hasPermission(Permissions.BANK_REMOVE_OTHER))
                            || (bank.isAdminBank() && player.hasPermission(Permissions.BANK_REMOVE_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
