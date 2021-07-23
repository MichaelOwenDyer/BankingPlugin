package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankLimits extends SubCommand.BankSubCommand {

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
        long allowedVolume = Utils.getBankVolumeLimit(p);
        String bankLimit = allowedBanks < 0 ? "∞" : "" + allowedBanks;
        String volumeLimit = allowedVolume < 0 ? "∞" : "" + allowedVolume;
        PLUGIN.debugf("%s is viewing their bank limits: %d / %s, max volume: %d", p.getName(), banksUsed, bankLimit, allowedVolume);
        p.sendMessage(Messages.get(Message.BANK_LIMIT,
                new Replacement(Placeholder.NUMBER_OF_BANKS, banksUsed),
                new Replacement(Placeholder.LIMIT, bankLimit),
                new Replacement(Placeholder.BANK_SIZE, volumeLimit)
        ));
        return true;
    }

}
