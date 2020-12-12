package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Messenger;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class BankRemoveall extends BankCommand.SubCommand implements ConfirmableSubCommand {

    BankRemoveall() {
        super("removeall", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_REMOVEALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_REMOVEALL;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to remove all banks");

        if (!sender.hasPermission(Permissions.BANK_REMOVEALL)) {
            plugin.debug(sender.getName() + " does not have permission to remove all banks");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_REMOVEALL));
            return true;
        }

        Set<Bank> banks = bankRepo.getBanks();

        if (banks.isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.BANKS_NOT_FOUND));
            return true;
        }

        int affectedAccounts = banks.stream().map(Bank::getAccounts).mapToInt(Collection::size).sum();
        if (sender instanceof Player && Config.confirmOnRemoveAll && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(LangUtils.getMessage(Message.BANK_CONFIRM_REMOVE,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, affectedAccounts)
            ));
            return true;
        }

        BankRemoveAllEvent event = new BankRemoveAllEvent(sender, banks);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank remove all event cancelled");
            return true;
        }

        sender.sendMessage(LangUtils.getMessage(Message.ALL_BANKS_REMOVED,
                new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, affectedAccounts)
        ));
        for (Bank bank : banks) {
            Messenger messenger = new Messenger(LangUtils.getMessage(Message.BANK_REMOVED,
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, () -> bank.getAccounts().size())
            ));
            messenger.addOfflineRecipient(bank.getTrustedPlayers());
            messenger.removeRecipient(sender);
            messenger.send();
        }
        banks.forEach(bank -> bankRepo.removeBank(bank, true));
        plugin.debug("Bank(s) " + Utils.map(banks, bank -> "#" + bank.getID()).toString() + " removed from the database.");
        return true;
    }

}
