package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountConfigureEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountConfigure extends PlayerSubCommand {

    public enum AccountField {
        MULTIPLIER_STAGE,
        REMAINING_OFFLINE_PAYOUTS;

        @Override
        public String toString() {
            return name().toLowerCase().replace('_', '-');
        }
    }

    AccountConfigure(BankingPlugin plugin) {
		super(plugin, "configure");
    }
    
    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_CONFIGURE
                .or(permissible -> plugin.getBankService().countByTrustedPlayer((Player) permissible) > 0);
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
    protected int getMinimumArguments() {
        return 2;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException {
        String property = args[0];
        AccountField field = Stream.of(AccountField.values())
                .filter(f -> f.toString().equalsIgnoreCase(property))
                .findFirst()
                .orElseThrow(() -> err(Message.NOT_A_PROPERTY.with(Placeholder.INPUT).as(property)));

        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw err(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(args[1]));
        }

        ClickAction.setAccountClickAction(player, account -> configure(player, account, field, value));
        player.sendMessage(Message.CLICK_ACCOUNT_CONFIGURE
                .with(Placeholder.PROPERTY).as(property)
                .and(Placeholder.VALUE).as(value)
                .translate(plugin));
    }

    private void configure(Player executor, Account account, AccountField field, int value) throws CommandExecutionException {
        ClickAction.remove(executor);

        if (Permissions.ACCOUNT_CONFIGURE.notOwnedBy(executor) && !account.getBank().isTrusted(executor))
            throw err(Message.NO_PERMISSION_ACCOUNT_CONFIGURE);

        switch (field) {

            case MULTIPLIER_STAGE:

                List<Integer> multipliers = plugin.config().interestMultipliers.at(account.getBank());
                value = Math.min(value, multipliers.size() - 1);
                account.setMultiplierStage(value);

                executor.sendMessage(Message.ACCOUNT_SET_INTEREST_MULTIPLIER
                        .with(Placeholder.INTEREST_MULTIPLIER).as(account.getInterestMultiplier(multipliers))
                        .translate(plugin));
                plugin.debugf("%s has set the multiplier stage of account #%d to %d",
                        executor.getName(), account.getID(), account.getInterestMultiplierStage());
                break;

            case REMAINING_OFFLINE_PAYOUTS:

                account.setRemainingOfflinePayouts(value);

                plugin.debugf("%s has set the remaining offline payouts of account #%d to %d.",
                        executor.getName(), account.getID(), account.getRemainingOfflinePayouts());
                executor.sendMessage(Message.ACCOUNT_SET_REMAINING_OFFLINE
                        .with(Placeholder.VALUE).as(account.getRemainingOfflinePayouts())
                        .translate(plugin));
                break;

        }
        plugin.getAccountService().update(account);
        new AccountConfigureEvent(executor, account, field, value).fire();
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Stream.of(AccountField.values())
                .map(AccountField::toString)
                .filter(field -> field.toLowerCase().contains(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }

}
