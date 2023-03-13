package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.gui.AccountListGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.util.Promise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        Promise.async(plugin, () -> Arrays.stream(args)
                .map(SubCommand::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
        ).then(owners -> new AccountListGUI(plugin, player, owners).open());
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        List<String> argList = Arrays.asList(args);
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !argList.contains(name))
                .filter(name -> containsIgnoreCase(name, args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

}
