package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.entity.Player;

public class BankLimits extends PlayerSubCommand {

    BankLimits(BankingPlugin plugin) {
		super(plugin, "limits");
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
    protected void execute(Player player, String[] args) {
        int banksUsed = plugin.getBankService().findByOwner(player).size();
        long allowedBanks = getPermissionLimit(player, Permission.BANK_NO_LIMIT, plugin.config().defaultBankLimit.get());
        long allowedVolume = getPermissionLimit(player, Permission.BANK_NO_SIZE_LIMIT, plugin.config().maximumBankVolume.get());
        String bankLimit = allowedBanks < 0 ? "∞" : "" + allowedBanks;
        String volumeLimit = allowedVolume < 0 ? "∞" : "" + allowedVolume;
        plugin.debugf("%s is viewing their bank limits: %d / %s, max volume: %d", player.getName(), banksUsed, bankLimit, allowedVolume);
        player.sendMessage(Message.BANK_LIMIT
                .with(Placeholder.NUMBER_OF_BANKS).as(banksUsed)
                .and(Placeholder.LIMIT).as(bankLimit)
                .and(Placeholder.BANK_SIZE).as(volumeLimit)
                .translate(plugin));
    }

}
