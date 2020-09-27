package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankLimits extends BankCommand.SubCommand {

    BankLimits() {
        super("limits", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.BANK_CREATE) ? Messages.COMMAND_USAGE_BANK_LIMITS : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int banksUsed = bankUtils.getNumberOfBanks(p);
        int allowedBanks = BankUtils.getBankLimit(p);
        long allowedVolume = BankUtils.getVolumeLimit(p);
        String bankLimit = allowedBanks < 0 ? "∞" : "" + allowedBanks;
        String volumeLimit = allowedVolume < 0 ? "∞" : Utils.format(allowedVolume);
        plugin.debugf("%s is viewing their bank limits: %d / %s, max volume: %d", p.getName(), banksUsed, bankLimit, allowedVolume);
        p.sendMessage(String.format(Messages.BANK_LIMIT, banksUsed, bankLimit, volumeLimit));
        return true;
    }

}
