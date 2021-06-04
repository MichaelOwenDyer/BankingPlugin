package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class BankRemoveAll extends BankCommand.SubCommand implements ConfirmableSubCommand {

    BankRemoveAll() {
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
        PLUGIN.debug(sender.getName() + " wants to remove all banks");

        if (!sender.hasPermission(Permissions.BANK_REMOVEALL)) {
            PLUGIN.debug(sender.getName() + " does not have permission to remove all banks");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_REMOVEALL));
            return true;
        }

        Set<Bank> banks = bankRepo.getAll();

        if (banks.isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.BANKS_NOT_FOUND));
            return true;
        }

        int affectedAccounts = banks.stream().map(Bank::getAccounts).mapToInt(Collection::size).sum();
        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(LangUtils.getMessage(Message.BANK_CONFIRM_REMOVE,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, affectedAccounts)
            ));
            return true;
        }

        BankRemoveAllEvent event = new BankRemoveAllEvent(sender, banks);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Bank remove all event cancelled");
            return true;
        }

        sender.sendMessage(LangUtils.getMessage(Message.ALL_BANKS_REMOVED,
                new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, affectedAccounts)
        ));
        for (Bank bank : banks) {
            MailingRoom mailingRoom = new MailingRoom(LangUtils.getMessage(Message.BANK_REMOVED,
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, () -> bank.getAccounts().size())
            ));
            mailingRoom.addOfflineRecipient(bank.getTrustedPlayers());
            mailingRoom.removeRecipient(sender);
            mailingRoom.send();
        }
        banks.forEach(bank -> bankRepo.remove(bank, true));
        PLUGIN.debug("Bank(s) " + Utils.map(banks, bank -> "#" + bank.getID()).toString() + " removed from the database.");
        return true;
    }

}
