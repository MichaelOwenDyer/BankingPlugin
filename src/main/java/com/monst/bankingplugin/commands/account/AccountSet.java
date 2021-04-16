package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountSet extends AccountCommand.SubCommand {

    AccountSet() {
        super("set", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_SET;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_SET;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to configure an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_SET)) {
            plugin.debug(sender.getName() + " does not have permission to configure an account");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_SET));
            return true;
        }

        if (args.length < 3)
            return false;

        String value;
        try {
            value = "" + Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(LangUtils.getMessage(Message.NOT_A_NUMBER, new Replacement(Placeholder.STRING, args[2])));
            return true;
        }

        String property = args[1].toLowerCase();
        switch (property) {
            case "multiplier":
                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.MULTIPLIER_STAGE, value));
                break;
            case "delay-until-next-payout":
                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.DELAY_UNTIL_NEXT_PAYOUT, value));
                break;
            case "remaining-offline-payouts":
                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.REMAINING_OFFLINE_PAYOUTS, value));
                break;
            case "remaining-offline-payouts-until-reset":
                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET, value));
                break;
            default:
                sender.sendMessage(LangUtils.getMessage(Message.NOT_A_PROPERTY, new Replacement(Placeholder.STRING, args[1])));
                return true;
        }
        sender.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_SET,
                new Replacement(Placeholder.PROPERTY, property),
                new Replacement(Placeholder.VALUE, value)
        ));
        return true;
    }

    public static void set(Player executor, Account account, AccountField field, String value) {

        int intValue = Integer.parseInt(value);
        boolean isRelative = value.startsWith("+") || value.startsWith("-");

        switch (field) {

            case MULTIPLIER_STAGE:

                value += isRelative ? account.getMultiplierStage() : 0;
                account.setMultiplierStage(intValue);

                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_MULTIPLIER,
                        new Replacement(Placeholder.MULTIPLIER, account::getRealMultiplier),
                        new Replacement(Placeholder.MULTIPLIER_STAGE, account::getMultiplierStage)
                ));
                plugin.debugf("%s has set the multiplier stage of account #%d to %d",
                        executor.getName(), account.getID(), account.getMultiplierStage());
                break;

            case DELAY_UNTIL_NEXT_PAYOUT:

                value += isRelative ? account.getDelayUntilNextPayout() : 0;
                account.setDelayUntilNextPayout(intValue);

                plugin.debugf("%s has set the interest delay of account #%d to %d.",
                        executor.getName(), account.getID(), account.getDelayUntilNextPayout());
                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_INTEREST_DELAY,
                        new Replacement(Placeholder.NUMBER, account::getDelayUntilNextPayout)
                ));
                break;

            case REMAINING_OFFLINE_PAYOUTS:

                value += isRelative ? account.getRemainingOfflinePayouts() : 0;
                account.setRemainingOfflinePayouts(intValue);

                plugin.debugf("%s has set the remaining offline payouts of account #%d to %d.",
                        executor.getName(), account.getID(), account.getRemainingOfflinePayouts());
                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_REMAINING_OFFLINE,
                        new Replacement(Placeholder.NUMBER, account::getRemainingOfflinePayouts)
                ));
                break;

            case REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET:

                value += isRelative ? account.getRemainingOfflinePayoutsUntilReset() : 0;
                account.setRemainingOfflinePayoutsUntilReset(intValue);

                plugin.debugf("%s has set the remaining offline payouts until reset of account #%d to %d.",
                        executor.getName(), account.getID(), account.getRemainingOfflinePayoutsUntilReset());
                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_REMAINING_OFFLINE_RESET,
                        new Replacement(Placeholder.NUMBER, account::getRemainingOfflinePayoutsUntilReset)
                ));
                break;

        }

        plugin.getAccountRepository().update(account, Callback.blank(), field);
        AccountConfigureEvent e = new AccountConfigureEvent(executor, account, field, value);
        Bukkit.getPluginManager().callEvent(e);
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2 || !sender.hasPermission(Permissions.ACCOUNT_SET))
            return Collections.emptyList();
        return Stream.of("multiplier", "delay-until-next-payout", "remaining-offline-payouts", "remaining-offline-payouts-until-reset")
                .filter(field -> Utils.containsIgnoreCase(field, args[1]))
                .collect(Collectors.toList());
    }

}
