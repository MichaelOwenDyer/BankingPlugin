package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountTrust extends AccountSubCommand {

    public AccountTrust() {
        super("trust", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? Messages.COMMAND_USAGE_ACCOUNT_TRUST : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length < 2)
            return false;

        plugin.debug(p.getName() + " wants to trust a player to an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRUST);
            return true;
        }
        OfflinePlayer playerToTrust = Utils.getPlayer(args[1]);
        if (playerToTrust == null) {
            p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
            return true;
        }

        p.sendMessage(String.format(Messages.CLICK_CHEST_TRUST, Utils.samePlayer(playerToTrust, p) ? "yourself" : playerToTrust.getName()));
        ClickType.setPlayerClickType(p, new ClickType.TrustClickType(playerToTrust));
        plugin.debug(p.getName() + " is trusting " + playerToTrust.getName() + " to an account");
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST_OTHER))
            onlinePlayers.remove(sender.getName());
        return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
    }

}
