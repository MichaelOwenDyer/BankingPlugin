package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankLimits extends SubCommand.BankSubCommand {

    BankLimits(BankingPlugin plugin) {
		super(plugin, "limits", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIMITS;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int banksUsed = plugin.getBankRepository().getOwnedBy(p).size();
        int allowedBanks = Utils.getBankLimit(p);
        long allowedVolume = Utils.getBankVolumeLimit(p);
        String bankLimit = allowedBanks < 0 ? "∞" : "" + allowedBanks;
        String volumeLimit = allowedVolume < 0 ? "∞" : "" + allowedVolume;
        plugin.debugf("%s is viewing their bank limits: %d / %s, max volume: %d", p.getName(), banksUsed, bankLimit, allowedVolume);
        p.sendMessage(Message.BANK_LIMIT
                .with(Placeholder.NUMBER_OF_BANKS).as(banksUsed)
                .and(Placeholder.LIMIT).as(bankLimit)
                .and(Placeholder.BANK_SIZE).as(volumeLimit)
                .translate());
        return true;
    }

}
