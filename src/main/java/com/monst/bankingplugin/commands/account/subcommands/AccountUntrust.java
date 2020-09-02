package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountUntrust extends AccountSubCommand {

    public AccountUntrust() {
        super("untrust", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length < 2)
            return false;

        plugin.debug(p.getName() + " wants to untrust a player from an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_UNTRUST);
            return true;
        }
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null) {
            p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
            return true;
        }

        p.sendMessage(String.format(Messages.CLICK_CHEST_UNTRUST, Utils.samePlayer(playerToUntrust, p) ? "yourself" : playerToUntrust.getName()));
        ClickType.setPlayerClickType(p, new ClickType.UntrustClickType(playerToUntrust));
        plugin.debug(p.getName() + " is untrusting " + playerToUntrust.getName() + " from an account");
        return true;
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? Messages.COMMAND_USAGE_ACCOUNT_UNTRUST : "";
    }

}
