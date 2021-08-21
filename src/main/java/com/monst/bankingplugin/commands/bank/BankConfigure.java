package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.bank.BankConfigureEvent;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankConfigure extends SubCommand.BankSubCommand {

    BankConfigure(BankingPlugin plugin) {
		super(plugin, "configure", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_CONFIGURE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to configure a bank");

        if (args.length < 3)
            return false;

        Bank bank = plugin.getBankRepository().getByIdentifier(args[1]);
        String fieldName = args[2];
        String value = Arrays.stream(args).skip(3).collect(Collectors.joining(" "));

        if (bank == null) {
            plugin.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }
        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            plugin.debug(sender.getName() + " does not have permission to configure another player's bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_SET_OTHER.translate());
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to configure an admin bank");
            sender.sendMessage(Message.NO_PERMISSION_BANK_SET_ADMIN.translate());
            return true;
        }

        BankField field = BankField.getByName(fieldName);
        if (field == null) {
            plugin.debug("No bank config field could be found with name " + fieldName);
            sender.sendMessage(Message.NOT_A_PROPERTY.with(Placeholder.INPUT).as(fieldName).translate());
            return true;
        }

        String previousValue = bank.get(field).getFormatted(true);

        try {
            if (!bank.set(field, value))
                sender.sendMessage(Message.BANK_PROPERTY_NOT_OVERRIDABLE
                        .with(Placeholder.PROPERTY).as(field.toString())
                        .translate());
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getLocalizedMessage());
            plugin.debugf("Could not parse argument: \"%s\"", e.getLocalizedMessage());
            return true;
        }

        String newValue = bank.get(field).getFormatted(true);

        plugin.debugf( "%s has changed %s at %s from %s to %s.",
                sender.getName(), field.toString(), bank.getName(), previousValue, newValue);
        String message = Message.BANK_PROPERTY_SET
                .with(Placeholder.PROPERTY).as(field.toString())
                .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .and(Placeholder.PREVIOUS_VALUE).as(previousValue)
                .and(Placeholder.VALUE).as(newValue)
                .translate();
        MailingRoom.draft(message).to(bank.getTrustedPlayers()).and(bank.getCustomers()).and(sender).send();

        new BankConfigureEvent(sender, bank, field, value, previousValue).fire();

        plugin.getBankRepository().update(bank, field);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankRepository().getAll().stream()
                    .filter(bank -> bank.isTrusted(player)
                            || (bank.isPlayerBank() && player.hasPermission(Permissions.BANK_SET_OTHER))
                            || (bank.isAdminBank() && player.hasPermission(Permissions.BANK_SET_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        Bank bank = plugin.getBankRepository().getByIdentifier(args[0]);
        if (bank == null)
            return Collections.emptyList();
        if (args.length == 2)
            return BankField.matchConfigurablePath(args[1]);
        BankField field = BankField.getByName(args[1]);
        if (field == null || args.length > 3)
            return Collections.emptyList();
        return Collections.singletonList(bank.get(field).getFormatted());
    }

}
