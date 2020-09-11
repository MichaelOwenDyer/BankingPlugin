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
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to configure an account");
        if (args.length < 2)
            return false;

        switch (args[1].toLowerCase()) {

            case "multiplier":
                if (args.length < 3)
                    return false;
                if (!p.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
                    p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
                    return true;
                }

                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
                    return true;
                }

                ClickType.setPlayerClickType(p, ClickType.set(AccountField.MULTIPLIER, args[2]));
                p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "set"));
                break;

            case "interest-delay":
                if (args.length < 3)
                    return false;
                if (p.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
                    p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
                    return true;
                }

                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(String.format(Messages.NOT_A_NUMBER, args[2]));
                }

                ClickType.setPlayerClickType(p, ClickType.set(AccountField.DELAY, args[2]));
                p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "set"));
                break;

            default:
                p.sendMessage(String.format(Messages.NOT_A_FIELD, args[1]));
        }
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();
        return Stream.of("multiplier", "interest-delay")
                .filter(field -> field.contains(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

    public static void set(Player executor, Account account, AccountField field, String value) {

        switch (field) {

            case MULTIPLIER:
                if (!executor.hasPermission(Permissions.ACCOUNT_SET_MULTIPLIER)) {
                    plugin.debugf("%s does not have permission to change %s's account multiplier",
                            executor.getName(), account.getOwner().getName());
                    executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_MULTIPLIER);
                    return;
                }

                if (value.startsWith("+") || value.startsWith("-"))
                    account.getStatus().setMultiplierStageRelative(Integer.parseInt(value));
                else
                    account.getStatus().setMultiplierStage(Integer.parseInt(value));

                executor.sendMessage(String.format(Messages.MULTIPLIER_SET, account.getStatus().getRealMultiplier()));
                plugin.debugf("%s has set an account multiplier stage to %d (#%d)%s",
                        executor.getName(), account.getStatus().getMultiplierStage(), account.getID(), (account.isCoowner(executor) ? " (is co-owner)" : ""));
                break;

            case DELAY:
                if (!executor.hasPermission(Permissions.ACCOUNT_SET_INTEREST_DELAY)) {
                    executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_INTEREST_DELAY);
                    return;
                }

                if (value.startsWith("+") || value.startsWith("-"))
                    account.getStatus().setInterestDelayRelative(Integer.parseInt(value)); // Set relative to current if value prefixed with + or -
                else
                    account.getStatus().setInterestDelay(Integer.parseInt(value));

                plugin.debugf("%s has set the interest delay of account #%d to %d.",
                        executor.getName(), account.getID(), account.getStatus().getDelayUntilNextPayout());
                executor.sendMessage(String.format(Messages.INTEREST_DELAY_SET, account.getStatus().getDelayUntilNextPayout()));
        }

        plugin.getAccountUtils().addAccount(account, true);
        AccountConfigureEvent e = new AccountConfigureEvent(executor, account, field, value);
        Bukkit.getPluginManager().callEvent(e);
    }

}
