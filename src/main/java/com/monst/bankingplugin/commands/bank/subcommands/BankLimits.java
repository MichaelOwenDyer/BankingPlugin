package com.monst.bankingplugin.commands.bank.subcommands;

import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankLimits extends BankSubCommand {

    public BankLimits() {
        super("limits", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int banksUsed = bankUtils.getNumberOfBanks(p);
        int bankLimit = BankUtils.getBankLimit(p);
        String limit = bankLimit < 0 ? "∞" : "" + bankLimit;
        plugin.debug(p.getName() + " is viewing their bank limits: " + banksUsed + " / " + limit);
        p.sendMessage(String.format(Messages.BANK_LIMIT, banksUsed, limit));
        return true;
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_LIMITS : "";
    }

}
