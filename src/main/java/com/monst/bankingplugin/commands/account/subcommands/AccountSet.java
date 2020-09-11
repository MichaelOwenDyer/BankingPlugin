package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountSet extends AccountSubCommand {

    public AccountSet() {
        super("set", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_SET) ? Messages.COMMAND_USAGE_ACCOUNT_SET : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return false;
        
        plugin.debug(sender.getName() + " wants to configure an account");
        if (!sender.hasPermission(Permissions.ACCOUNT_SET)) {
            plugin.debug(sender.getName() + " does not have permission to configure an account");
            sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET);
            return true;
        }

        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
            return true;
        }

        switch (args[1].toLowerCase()) {

            case "multiplier":

                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.MULTIPLIER, args[2]));
                break;

            case "delay-until-next-payout":

                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.DELAY_UNTIL_NEXT_PAYOUT, args[2]));
                break;

            case "remaining-offline-payouts":

                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.REMAINING_OFFLINE_PAYOUTS, args[2]));
                break;

            case "remaining-offline-payouts-until-reset":

                ClickType.setPlayerClickType(((Player) sender), ClickType.set(AccountField.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET, args[2]));
                break;

            default:

                sender.sendMessage(String.format(Messages.NOT_A_FIELD, args[1]));
                return true;
        }
        sender.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "set"));
        return true;
    }

    public static void set(Player executor, Account account, AccountField field, String value) {

        int intValue = Integer.parseInt(value);
        boolean isRelative = value.startsWith("+") || value.startsWith("-");

        switch (field) {

            case MULTIPLIER:

                value += isRelative ? account.getStatus().getMultiplierStage() : 0;
                account.getStatus().setMultiplierStage(intValue);

                executor.sendMessage(String.format(Messages.MULTIPLIER_SET, account.getStatus().getRealMultiplier()));
                plugin.debugf("%s has set the multiplier stage of account #%d to %d",
                        executor.getName(), account.getID(), account.getStatus().getMultiplierStage());
                break;

            case DELAY_UNTIL_NEXT_PAYOUT:

                value += isRelative ? account.getStatus().getDelayUntilNextPayout() : 0;
                account.getStatus().setDelayUntilNextPayout(intValue);

                plugin.debugf("%s has set the interest delay of account #%d to %d.",
                        executor.getName(), account.getID(), account.getStatus().getDelayUntilNextPayout());
                executor.sendMessage(String.format(Messages.INTEREST_DELAY_SET,
                        account.getStatus().getDelayUntilNextPayout()));
                break;

            case REMAINING_OFFLINE_PAYOUTS:

                value += isRelative ? account.getStatus().getRemainingOfflinePayouts() : 0;
                account.getStatus().setRemainingOfflinePayouts(intValue);

                plugin.debugf("%s has set the remaining offline payouts of account #%d to %d.",
                        executor.getName(), account.getID(), account.getStatus().getRemainingOfflinePayouts());
                executor.sendMessage(String.format(Messages.REMAINING_OFFLINE_PAYOUTS_SET,
                        account.getStatus().getRemainingOfflinePayouts()));
                break;

            case REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET:

                value += isRelative ? account.getStatus().getRemainingOfflinePayoutsUntilReset() : 0;
                account.getStatus().setRemainingOfflinePayoutsUntilReset(intValue);

                plugin.debugf("%s has set the remaining offline payouts until reset of account #%d to %d.",
                        executor.getName(), account.getID(), account.getStatus().getRemainingOfflinePayoutsUntilReset());
                executor.sendMessage(String.format(Messages.REMAINING_OFFLINE_PAYOUTS_UNTIL_RESET_SET,
                        account.getStatus().getRemainingOfflinePayoutsUntilReset()));
                break;

        }

        plugin.getAccountUtils().addAccount(account, true);
        AccountConfigureEvent e = new AccountConfigureEvent(executor, account, field, value);
        Bukkit.getPluginManager().callEvent(e);
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2 || !sender.hasPermission(Permissions.ACCOUNT_SET))
            return Collections.emptyList();
        return Stream.of("multiplier", "delay-until-next-payout", "remaining-offline-payouts", "remaining-offline-payouts-until-reset")
                .filter(field -> field.contains(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
