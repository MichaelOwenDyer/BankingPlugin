package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.entity.Player;

public class BankLimits extends PlayerSubCommand {

    BankLimits(BankingPlugin plugin) {
		super(plugin, "limits");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_LIMITS;
    }

    @Override
    protected void execute(Player player, String[] args) {
        int banksUsed = plugin.getBankService().countByOwner(player);
        String bankLimit = getBankLimit(player).map(String::valueOf).orElse("∞");
        String volumeLimit = getBankVolumeLimit(player).map(String::valueOf).orElse("∞");
        plugin.debug("%s is viewing their bank limits: %d / %s, max volume: %s", player.getName(),
                banksUsed, bankLimit, volumeLimit);
        player.sendMessage(Message.BANK_LIMIT
                .with(Placeholder.NUMBER_OF_BANKS).as(banksUsed)
                .and(Placeholder.LIMIT).as(bankLimit)
                .and(Placeholder.BANK_SIZE).as(volumeLimit)
                .translate(plugin));
    }

}
