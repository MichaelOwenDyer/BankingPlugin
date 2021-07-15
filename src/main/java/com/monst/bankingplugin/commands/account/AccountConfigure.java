package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountConfigure extends AccountCommand.SubCommand {

    AccountConfigure() {
        super("configure", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_CONFIGURE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CONFIGURE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player executor = (Player) sender;
        PLUGIN.debug(executor.getName() + " wants to configure an account");

        if (!executor.hasPermission(Permissions.ACCOUNT_CONFIGURE)) {
            PLUGIN.debug(executor.getName() + " does not have permission to configure an account");
            executor.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CONFIGURE));
            return true;
        }

        if (args.length < 3)
            return false;

        String property = args[1];
        AccountField field = AccountField.getByName(property);
        if (field == null) {
            executor.sendMessage(LangUtils.getMessage(Message.NOT_A_PROPERTY, new Replacement(Placeholder.INPUT, args[1])));
            return true;
        }

        String value;
        try {
            value = "" + Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            executor.sendMessage(LangUtils.getMessage(Message.NOT_A_NUMBER, new Replacement(Placeholder.INPUT, args[2])));
            return true;
        }

        ClickType.setConfigureClickType(executor, field, value);
        executor.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_CONFIGURE,
                new Replacement(Placeholder.PROPERTY, property),
                new Replacement(Placeholder.VALUE, value)
        ));
        return true;
    }

    public static void configure(Player executor, Account account, AccountField field, String value) {
        ClickType.removeClickType(executor);

        int intValue = Integer.parseInt(value);
        boolean isRelative = value.startsWith("+") || value.startsWith("-");

        switch (field) {

            case MULTIPLIER_STAGE:

                intValue += isRelative ? account.getMultiplierStage() : 0;
                account.setMultiplierStage(intValue);

                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_MULTIPLIER,
                        new Replacement(Placeholder.MULTIPLIER, account::getRealMultiplier),
                        new Replacement(Placeholder.MULTIPLIER_STAGE, account::getMultiplierStage)
                ));
                PLUGIN.debugf("%s has set the multiplier stage of account #%d to %d",
                        executor.getName(), account.getID(), account.getMultiplierStage());
                break;

            case DELAY_UNTIL_NEXT_PAYOUT:

                intValue += isRelative ? account.getDelayUntilNextPayout() : 0;
                account.setDelayUntilNextPayout(intValue);

                PLUGIN.debugf("%s has set the interest delay of account #%d to %d.",
                        executor.getName(), account.getID(), account.getDelayUntilNextPayout());
                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_INTEREST_DELAY,
                        new Replacement(Placeholder.NUMBER, account::getDelayUntilNextPayout)
                ));
                break;

            case REMAINING_OFFLINE_PAYOUTS:

                intValue += isRelative ? account.getRemainingOfflinePayouts() : 0;
                account.setRemainingOfflinePayouts(intValue);

                PLUGIN.debugf("%s has set the remaining offline payouts of account #%d to %d.",
                        executor.getName(), account.getID(), account.getRemainingOfflinePayouts());
                executor.sendMessage(LangUtils.getMessage(Message.ACCOUNT_SET_REMAINING_OFFLINE,
                        new Replacement(Placeholder.NUMBER, account::getRemainingOfflinePayouts)
                ));
                break;

        }

        PLUGIN.getAccountRepository().update(account, field);
        new AccountConfigureEvent(executor, account, field, value).fire();
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2 || !sender.hasPermission(Permissions.ACCOUNT_CONFIGURE))
            return Collections.emptyList();
        return Stream.of(AccountField.MULTIPLIER_STAGE, AccountField.DELAY_UNTIL_NEXT_PAYOUT, AccountField.REMAINING_OFFLINE_PAYOUTS)
                .map(AccountField::toString)
                .filter(field -> Utils.containsIgnoreCase(field, args[1]))
                .collect(Collectors.toList());
    }

}
