package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankLimits extends BankCommand.SubCommand {

    BankLimits() {
        super("limits", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIMITS;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int banksUsed = bankRepo.getOwnedBy(p).size();
        int allowedBanks = Utils.getBankLimit(p);
        long allowedVolume = getVolumeLimit(p);
        String bankLimit = allowedBanks < 0 ? "∞" : "" + allowedBanks;
        String volumeLimit = allowedVolume < 0 ? "∞" : "" + allowedVolume;
        plugin.debugf("%s is viewing their bank limits: %d / %s, max volume: %d", p.getName(), banksUsed, bankLimit, allowedVolume);
        p.sendMessage(LangUtils.getMessage(Message.BANK_LIMIT,
                new Replacement(Placeholder.NUMBER_OF_BANKS, banksUsed),
                new Replacement(Placeholder.LIMIT, bankLimit),
                new Replacement(Placeholder.BANK_SIZE, volumeLimit)
        ));
        return true;
    }

}
