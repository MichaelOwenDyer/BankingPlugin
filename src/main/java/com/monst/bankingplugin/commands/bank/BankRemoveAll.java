package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.PlayerCache;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class BankRemoveAll extends SubCommand.BankSubCommand {

    BankRemoveAll(BankingPlugin plugin) {
        super(plugin, "removeall", false);
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_REMOVEALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_REMOVEALL;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_REMOVEALL;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Set<Bank> banks = plugin.getBankRepository().getAll();
        if (banks.isEmpty()) {
            sender.sendMessage(Message.BANKS_NOT_FOUND.translate());
            return true;
        }

        int affectedAccounts = banks.stream().map(Bank::getAccounts).mapToInt(Collection::size).sum();
        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !PlayerCache.put((Player) sender, banks)) {
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
            String message = Message.BANK_REMOVED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(bank.getAccounts().size())
                    .translate();
            MailingRoom.draft(message).to(bank.getTrustedPlayers()).and(sender).send();
        }
        banks.forEach(bank -> plugin.getBankRepository().remove(bank, true));
        plugin.debugf("Bank(s) %s removed from the database.", Utils.map(banks, bank -> "#" + bank.getID()));
        return true;
    }

}
