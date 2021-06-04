package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveAllEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountRemoveall extends AccountCommand.SubCommand implements ConfirmableSubCommand {

    AccountRemoveall() {
        super("removeall", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_REMOVEALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_REMOVE_ALL;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        PLUGIN.debug(sender.getName() + " wants to remove all accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL)) {
            PLUGIN.debug(sender.getName() + " does not have permission to remove all accounts");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_REMOVEALL));
            return true;
        }

        Set<Account> accounts;

        if (args.length == 1) {
            accounts = accountRepo.getAll();
        } else {
            Map<String, Player> namePlayerMap = PLUGIN.getServer().getOnlinePlayers().stream().collect(
                    Collectors.toMap(
                            HumanEntity::getName,
                            e -> e
                    )
            );
            Set<OfflinePlayer> owners = new HashSet<>();
            for (String arg : args) {
                if (namePlayerMap.containsKey(arg))
                    owners.add(namePlayerMap.get(arg));
                else {
                    OfflinePlayer player = Utils.getPlayer(arg);
                    if (player != null)
                        owners.add(player);
                }
            }
            accounts = accountRepo.getMatching(a -> owners.contains(a.getOwner()));
        }

        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNTS_NOT_FOUND));
            return true;
        }

        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CONFIRM_REMOVE_ALL,
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accounts::size)
            ));
            sender.sendMessage(LangUtils.getMessage(Message.EXECUTE_AGAIN_TO_CONFIRM));
            return true;
        }

        AccountRemoveAllEvent event = new AccountRemoveAllEvent(sender, accounts);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Removeall event cancelled");
            return true;
        }
        PLUGIN.debug(sender.getName() + " removed account(s) " + Utils.map(accounts, a -> "#" + a.getID()).toString());
        sender.sendMessage(LangUtils.getMessage(Message.ALL_ACCOUNTS_REMOVED,
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accounts::size)
        ));
        accounts.forEach(a -> accountRepo.remove(a, true));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(PLUGIN), name -> !argList.contains(name));
    }

}
