package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
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
        return Permissions.BANK_CREATE;
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
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException {
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        if (bank.isAdminBank() && Permissions.BANK_CONFIGURE_ADMIN.notOwnedBy(sender))
            throw err(Message.NO_PERMISSION_BANK_RENAME_ADMIN);

        if (!(bank.isAdminBank() || (sender instanceof Player && bank.isTrusted((Player) sender))
                || Permissions.BANK_CONFIGURE_OTHER.ownedBy(sender)))
            throw err(Message.NO_PERMISSION_BANK_RENAME_OTHER);

        String newName = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        Bank bankWithSameName = plugin.getBankService().findByName(newName);
        if (bankWithSameName != null && !bankWithSameName.equals(bank))
            throw err(Message.NAME_NOT_UNIQUE.with(Placeholder.BANK_NAME).as(newName));

        if (!plugin.config().nameRegex.allows(newName))
            throw err(Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(newName)
                    .and(Placeholder.PATTERN).as(plugin.config().nameRegex.get()));

        bank.setName(newName);
        sender.sendMessage(Message.NAME_CHANGED.with(Placeholder.BANK_NAME).as(newName).translate(plugin));
        plugin.debug("%s is changing the name of bank %s", sender.getName(), bank);
        plugin.getBankService().update(bank);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService()
                    .findNamesByPlayerAllowedToModify(player,
                            Permissions.BANK_CONFIGURE_OTHER.ownedBy(player),
                            Permissions.BANK_CONFIGURE_ADMIN.ownedBy(player), false)
                    .stream()
                    .filter(name -> containsIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
