package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.bank.BankConfigureEvent;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankConfigure extends BankCommand.SubCommand {

    BankConfigure() {
        super("configure", false);
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
        PLUGIN.debug(sender.getName() + " wants to configure a bank");

        if (args.length < 3)
            return false;

        Bank bank = bankRepo.getByIdentifier(args[1]);
        String fieldName = args[2];
        String value = Arrays.stream(args).skip(3).collect(Collectors.joining(" "));

        if (bank == null) {
            PLUGIN.debugf("Couldn't find bank with name or ID %s", args[1]);
            sender.sendMessage(LangUtils.getMessage(Message.BANK_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }
        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            PLUGIN.debug(sender.getName() + " does not have permission to configure another player's bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_SET_OTHER));
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            PLUGIN.debug(sender.getName() + " does not have permission to configure an admin bank");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_BANK_SET_ADMIN));
            return true;
        }

        BankField field = BankField.getByName(fieldName);
        if (field == null) {
            PLUGIN.debug("No bank config field could be found with name " + fieldName);
            sender.sendMessage(LangUtils.getMessage(Message.NOT_A_PROPERTY, new Replacement(Placeholder.STRING, fieldName)));
            return true;
        }

        String previousValue = bank.get(field).getFormatted(true);

        try {
            if (!bank.set(field, value))
                sender.sendMessage(LangUtils.getMessage(Message.BANK_PROPERTY_NOT_OVERRIDABLE,
                        new Replacement(Placeholder.PROPERTY, field::toString)
                ));
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getLocalizedMessage());
            return true;
        }

        String newValue = bank.get(field).getFormatted(true);

        PLUGIN.debugf( "%s has changed %s at %s from %s to %s.",
                sender.getName(), field.toString(), bank.getName(), previousValue, newValue);
        MailingRoom mailingRoom = new MailingRoom(LangUtils.getMessage(Message.BANK_PROPERTY_SET,
                new Replacement(Placeholder.PROPERTY, field::toString),
                new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                new Replacement(Placeholder.PREVIOUS_VALUE, previousValue),
                new Replacement(Placeholder.VALUE, newValue)
        ));
        mailingRoom.addOfflineRecipient(bank.getTrustedPlayers());
        mailingRoom.addOfflineRecipient(bank.getCustomers());
        mailingRoom.addRecipient(sender);
        mailingRoom.send(); // TODO: Mail as well?

        new BankConfigureEvent(sender, bank, field, value, previousValue).fire();

        bankRepo.update(bank, field);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return bankRepo.getAll().stream()
                    .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                            || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                            || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[1]))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 3 && bankRepo.getByIdentifier(args[1]) != null) {
            return BankField.streamConfigurable()
                    .map(BankField::toString)
                    .filter(name -> Utils.containsIgnoreCase(name, args[2]))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 4) {
            Bank bank = bankRepo.getByIdentifier(args[1]);
            BankField field = BankField.getByName(args[2]);
            if (bank != null && field != null)
                return Collections.singletonList(bank.get(field).getFormatted());
        }
        return Collections.emptyList();
    }

}
