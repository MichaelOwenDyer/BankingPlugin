package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messenger;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
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

        if (sender instanceof Player && Config.confirmOnRemove && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(LangUtils.getMessage(Message.BANK_CONFIRM_REMOVE,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, 1),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, () -> bank.getAccounts().size())
            ));
            return true;
        }

        if (sender instanceof Player) {
            double creationPrice = bank.isAdminBank() ? Config.bankCreationPriceAdmin : Config.bankCreationPricePlayer;
            boolean reimburse = bank.isAdminBank() ? Config.reimburseBankCreationAdmin : Config.reimburseBankCreationPlayer;
            creationPrice *= reimburse ? 1 : 0;

            Player executor = (Player) sender;
            if (creationPrice > 0 && (bank.isAdminBank() || bank.isOwner(executor))) {
                double finalCreationPrice = creationPrice;
                Utils.depositPlayer(executor.getPlayer(), finalCreationPrice, Callback.of(plugin,
                        result -> executor.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                                new Replacement(Placeholder.AMOUNT, finalCreationPrice)
                        )),
                        error -> executor.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                                new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                        ))
                ));
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
        Messenger messenger = new Messenger(LangUtils.getMessage(Message.BANK_REMOVED,
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accountsRemoved)
        ));
        messenger.addOfflineRecipient(bank.getTrustedPlayers());
        messenger.addOfflineRecipient(bank.getCustomers());
        messenger.addRecipient(sender);
        messenger.send();
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
