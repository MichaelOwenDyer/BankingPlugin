package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.command.CommandExecutionException;
import com.monst.bankingplugin.gui.BankGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankInfo extends PlayerSubCommand {

    BankInfo(BankingPlugin plugin) {
		super(plugin, "info");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_INFO;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException {
        Bank bank;
        if (args.length == 0) {
            bank = plugin.getBankService().findContaining(player);
            if (bank == null)
                throw err(Message.MUST_STAND_IN_OR_SPECIFY_BANK);
        } else {
            bank = plugin.getBankService().findByName(args[0]);
            if (bank == null)
                throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));
        }
        plugin.debug("%s is viewing GUI of bank %s", player.getName(), bank);
        new BankGUI(plugin, player, bank).open();
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService().findAllNames()
                    .stream()
                    .filter(name -> containsIgnoreCase(name, args[0]))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
