package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountConfigure extends SubCommand {

    AccountConfigure(BankingPlugin plugin) {
		super(plugin, "configure", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_CONFIGURE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CONFIGURE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_CONFIGURE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2)
            return false;

        Player p = (Player) sender;
        String property = args[0];
        AccountField field = AccountField.getByName(property);
        if (field == null) {
            p.sendMessage(Message.NOT_A_PROPERTY.with(Placeholder.INPUT).as(property).translate());
            return true;
        }

        int value;
        try {
            value = Parser.parseInt(args[1]);
        } catch (IntegerParseException e) {
            p.sendMessage(e.getLocalizedMessage());
            return true;
        }

        ClickType.setConfigureClickType(p, field, value);
        p.sendMessage(Message.CLICK_ACCOUNT_CONFIGURE
                .with(Placeholder.PROPERTY).as(property)
                .and(Placeholder.VALUE).as(value)
                .translate());
        return true;
    }

    public static void configure(BankingPlugin plugin, Player executor, Account account, AccountField field, int value) {
        ClickType.removeClickType(executor);

        switch (field) {

            case MULTIPLIER_STAGE:

                account.setMultiplierStage(value);

                executor.sendMessage(Message.ACCOUNT_SET_MULTIPLIER
                        .with(Placeholder.MULTIPLIER).as(account.getRealMultiplier())
                        .and(Placeholder.MULTIPLIER_STAGE).as(account.getMultiplierStage())
                        .translate());
                plugin.debugf("%s has set the multiplier stage of account #%d to %d",
                        executor.getName(), account.getID(), account.getMultiplierStage());
                break;

            case DELAY_UNTIL_NEXT_PAYOUT:

                account.setDelayUntilNextPayout(value);

                plugin.debugf("%s has set the interest delay of account #%d to %d.",
                        executor.getName(), account.getID(), account.getDelayUntilNextPayout());
                executor.sendMessage(Message.ACCOUNT_SET_INTEREST_DELAY
                        .with(Placeholder.NUMBER).as(account.getDelayUntilNextPayout())
                        .translate());
                break;

            case REMAINING_OFFLINE_PAYOUTS:

                account.setRemainingOfflinePayouts(value);

                plugin.debugf("%s has set the remaining offline payouts of account #%d to %d.",
                        executor.getName(), account.getID(), account.getRemainingOfflinePayouts());
                executor.sendMessage(Message.ACCOUNT_SET_REMAINING_OFFLINE
                        .with(Placeholder.NUMBER).as(account.getRemainingOfflinePayouts())
                        .translate());
                break;

        }

        plugin.getAccountRepository().update(account, field);
        new AccountConfigureEvent(executor, account, field, value).fire();
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1 || Permission.ACCOUNT_CONFIGURE.notOwnedBy(player))
            return Collections.emptyList();
        return Stream.of(AccountField.MULTIPLIER_STAGE, AccountField.DELAY_UNTIL_NEXT_PAYOUT, AccountField.REMAINING_OFFLINE_PAYOUTS)
                .map(AccountField::toString)
                .filter(field -> Utils.containsIgnoreCase(field, args[0]))
                .collect(Collectors.toList());
    }

}
