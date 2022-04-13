package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.gui.AccountListGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountList extends PlayerSubCommand {

    AccountList(BankingPlugin plugin) {
		super(plugin, "list");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (Permission.ACCOUNT_LIST_OTHER.notOwnedBy(player))
            new AccountListGUI(plugin, callback -> plugin.getAccountService().findByTrustedPlayer(player, callback)).open(player);
        else if (args.length == 0)
            new AccountListGUI(plugin, callback -> plugin.getAccountService().findAll(callback)).open(player);
        else {
            Set<OfflinePlayer> owners = Arrays.stream(args)
                    .map(Utils::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            new AccountListGUI(plugin, callback -> plugin.getAccountService().findByOwnerIn(owners, callback)).open(player);
        }
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        List<String> argList = Arrays.asList(args);
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
