package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.bank.BankConfigureEvent;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankSet extends BankSubCommand {

    public BankSet() {
        super("set", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_SET : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to configure a bank");

        if (args.length < 3)
            return false;

        Bank bank = bankUtils.lookupBank(args[1]);
        String fieldName = args[2];
        StringBuilder sb = new StringBuilder(32);
        if (args.length > 3)
            sb.append(args[3]);
        for (int i = 4; i < args.length; i++)
            sb.append(" ").append(args[i]);
        String value = sb.toString();

        if (bank == null) {
            plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
            sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
            return true;
        }
        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
                || sender.hasPermission(Permissions.BANK_SET_OTHER))) {
            plugin.debug(sender.getName() + " does not have permission to configure another player's bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_OTHER);
            return true;
        }
        if (bank.isAdminBank() && !sender.hasPermission(Permissions.BANK_SET_ADMIN)) {
            plugin.debug(sender.getName() + " does not have permission to configure an admin bank");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_SET_ADMIN);
            return true;
        }

        BankField field = BankField.getByName(fieldName);
        if (field == null) {
            plugin.debug("No bank config field could be found with name " + fieldName);
            sender.sendMessage(String.format(Messages.NOT_A_FIELD, fieldName));
            return true;
        }

        String previousValue = bank.getFormatted(field);
        Callback<String> callback = Callback.of(plugin,
            result -> {
                plugin.debug(sender.getName() + " has changed " + field.getName() + " at " + bank.getName() + " from " + previousValue + " to " + result);
                sender.sendMessage(String.format(Messages.BANK_FIELD_SET, "You", field.getName(), previousValue, result, bank.getColorizedName()));
                Set<OfflinePlayer> toNotify = Utils.mergeCollections(bank.getTrustedPlayers(), bank.getCustomers());
                if (sender instanceof Player)
                    toNotify.remove(sender);
                Utils.notifyPlayers(String.format(Messages.BANK_FIELD_SET, sender.getName(), field.getName(),
                        previousValue, result, bank.getColorizedName()), toNotify
                );
            },
            throwable -> sender.sendMessage(((ArgumentParseException) throwable).getErrorMessage()));

        if (!bank.set(field, value, callback)) {
            sender.sendMessage(Messages.FIELD_NOT_OVERRIDABLE);
            return true;
        }

        BankConfigureEvent e = new BankConfigureEvent(sender, bank, field, previousValue, value);
        Bukkit.getPluginManager().callEvent(e);

        if (field == BankField.INTEREST_PAYOUT_TIMES)
            plugin.getScheduler().schedulePayouts(bank);

        bankUtils.addBank(bank, true);
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return bankUtils.getBanksCopy().stream()
                    .filter(bank -> (sender instanceof Player && bank.isTrusted((Player) sender))
                            || (bank.isPlayerBank() && sender.hasPermission(Permissions.BANK_SET_OTHER))
                            || (bank.isAdminBank() && sender.hasPermission(Permissions.BANK_SET_ADMIN)))
                    .map(Bank::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 3 && bankUtils.lookupBank(args[1]) != null)
            return BankField.stream()
                    .filter(BankField::isOverrideAllowed)
                    .map(BankField::getName)
                    .filter(name -> name.contains(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        else if (args.length == 4) {
            Bank bank = bankUtils.lookupBank(args[1]);
            BankField field = BankField.getByName(args[2]);
            if (bank != null && field != null)
                return Collections.singletonList(bank.getFormatted(field));
        }
        return Collections.emptyList();
    }

}
