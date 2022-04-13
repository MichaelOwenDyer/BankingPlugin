package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankRename extends SubCommand {

    BankRename(BankingPlugin plugin) {
		super(plugin, "rename");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_RENAME;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_RENAME;
    }

    @Override
    protected int getMinimumArguments() {
        return 2;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws ExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        if (bank.isAdminBank() && Permission.BANK_CONFIGURE_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_RENAME_ADMIN);

        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
                || Permission.BANK_CONFIGURE_OTHER.ownedBy(sender)))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_RENAME_OTHER);

        String newName = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        Bank bankWithSameName = plugin.getBankService().findByName(newName);
        if (bankWithSameName != null && !bankWithSameName.equals(bank))
            throw new ExecutionException(plugin, Message.NAME_NOT_UNIQUE.with(Placeholder.BANK_NAME).as(newName));

        if (plugin.config().nameRegex.doesNotMatch(newName))
            throw new ExecutionException(plugin, Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(newName)
                    .and(Placeholder.PATTERN).as(plugin.config().nameRegex.get()));

        plugin.debugf("%s is changing the name of bank #%d to %s", sender.getName(), bank.getID(), newName);
        sender.sendMessage(Message.NAME_CHANGED.with(Placeholder.BANK_NAME).as(newName).translate(plugin));
        bank.setName(newName);
        plugin.getBankService().update(bank);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findByPlayerAllowedToModify(player, Permission.BANK_CONFIGURE_OTHER, Permission.BANK_CONFIGURE_ADMIN, false)
                    .stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
