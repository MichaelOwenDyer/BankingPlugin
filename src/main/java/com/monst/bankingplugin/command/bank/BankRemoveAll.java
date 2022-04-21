package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BankRemoveAll extends SubCommand {

    BankRemoveAll(BankingPlugin plugin) {
        super(plugin, "removeall");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_REMOVE_ALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_REMOVE_ALL;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_REMOVE_ALL;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        List<Bank> banks = plugin.getBankService().findAll();
        if (banks.isEmpty())
            throw new ExecutionException(plugin, Message.BANKS_NOT_FOUND);

        List<Account> accounts = plugin.getAccountService().findByBanks(banks);
        if (sender instanceof Player && plugin.config().confirmOnRemoveAll.get()
                && isFirstUsage((Player) sender, Objects.hash("removeAll", new HashSet<>(banks)))) {
            sender.sendMessage(Message.ABOUT_TO_REMOVE_ALL_BANKS
                    .with(Placeholder.NUMBER_OF_BANKS).as(banks.size())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate(plugin));
            return;
        }

        new BankRemoveAllEvent(sender, banks).fire();

        if (!accounts.isEmpty()) {
            plugin.getAccountService().removeAll(accounts);
            plugin.debugf("Account(s) %s removed from the database.", accounts.stream().map(Account::getID).collect(Collectors.toList()));
            accounts.forEach(Account::resetChestTitle);
        }
        plugin.getBankService().removeAll(banks);
        plugin.debugf("Bank(s) %s removed from the database.", banks.stream().map(Bank::getID).collect(Collectors.toList()));
        sender.sendMessage(Message.ALL_BANKS_REMOVED
                .with(Placeholder.NUMBER_OF_BANKS).as(banks.size())
                .and(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                .translate(plugin));
        for (Bank bank : banks) {
            String message = Message.BANK_REMOVED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(bank.getNumberOfAccounts())
                    .translate(plugin);
            bank.getMailingList(sender).forEach(player -> player.sendMessage(message));
        }
    }

}
