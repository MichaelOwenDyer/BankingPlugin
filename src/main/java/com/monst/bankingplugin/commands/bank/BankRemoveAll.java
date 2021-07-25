package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class BankRemoveAll extends SubCommand.BankSubCommand implements ConfirmableSubCommand {

    BankRemoveAll(BankingPlugin plugin) {
		super(plugin, "removeall", false);
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
            sender.sendMessage(Message.NO_PERMISSION_BANK_REMOVEALL.translate());
            return true;
        }

        Set<Bank> banks = plugin.getBankRepository().getAll();

        if (banks.isEmpty()) {
            sender.sendMessage(Message.BANKS_NOT_FOUND.translate());
            return true;
        }

        int affectedAccounts = banks.stream().map(Bank::getAccounts).mapToInt(Collection::size).sum();
        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(Message.BANK_CONFIRM_REMOVE
                    .with(Placeholder.NUMBER_OF_BANKS).as(banks.size())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(affectedAccounts)
                    .translate());
            return true;
        }

        BankRemoveAllEvent event = new BankRemoveAllEvent(sender, banks);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Bank remove all event cancelled");
            return true;
        }

        sender.sendMessage(Message.ALL_BANKS_REMOVED
                .with(Placeholder.NUMBER_OF_BANKS).as(banks.size())
                .and(Placeholder.NUMBER_OF_ACCOUNTS).as(affectedAccounts)
                .translate());
        for (Bank bank : banks) {
            MailingRoom mailingRoom = new MailingRoom(Message.BANK_REMOVED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(bank.getAccounts().size())
                    .translate());
            mailingRoom.addOfflineRecipient(bank.getTrustedPlayers());
            mailingRoom.removeRecipient(sender);
            mailingRoom.send();
        }
        banks.forEach(bank -> plugin.getBankRepository().remove(bank, true));
        plugin.debug("Bank(s) " + Utils.map(banks, bank -> "#" + bank.getID()).toString() + " removed from the database.");
        return true;
    }

}
