package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BankSubCommand extends BankingPluginSubCommand {

    static final BankUtils bankUtils = plugin.getBankUtils();

    public BankSubCommand(String name, boolean playerCommand) {
        super(name, playerCommand);
    }

    Bank getBank(CommandSender sender, String[] args) {
        Bank bank = null;
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                bank = bankUtils.getBank(p.getLocation());
                if (bank == null) {
                    plugin.debug(p.getName() + " wasn't standing in a bank");
                    p.sendMessage(Messages.NOT_STANDING_IN_BANK);
                }
            } else {
                sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
            }
        } else {
            bank = bankUtils.lookupBank(args[1]);
            if (bank == null) {
                plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
                sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
            }
        }
        return bank;
    }

}
