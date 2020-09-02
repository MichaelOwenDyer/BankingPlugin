package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.events.account.AccountPreCreateEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountCreate extends AccountSubCommand {

    public AccountCreate() {
        super("create", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_CREATE : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create an account");

        boolean forSelf = args.length == 1;
        boolean hasPermission = hasPermission(p, forSelf ? Permissions.ACCOUNT_CREATE : Permissions.ACCOUNT_CREATE_OTHER);

        if (!hasPermission) {
            if (!forSelf) {
                p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE_OTHER);
                plugin.debug(p.getName() + " is not permitted to create an account in another player's name");
                return true;
            }
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_CREATE);
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        OfflinePlayer owner = forSelf ? p.getPlayer() : Utils.getPlayer(args[1]);
        if (!forSelf && owner == null) {
            p.sendMessage(Messages.PLAYER_NOT_FOUND);
            plugin.debug("Could not find player with name \"" + args[1] + "\"");
            return true;
        }

        if (forSelf) {
            int limit = accountUtils.getAccountLimit(p);
            if (limit != -1 && accountUtils.getNumberOfAccounts(p) >= limit) {
                p.sendMessage(Messages.ACCOUNT_LIMIT_REACHED);
                plugin.debug(p.getName() + " has reached their account limit");
                return true;
            }
        }

        AccountPreCreateEvent event = new AccountPreCreateEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-create event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to create an account");
        p.sendMessage(Messages.CLICK_CHEST_CREATE);
        ClickType.setPlayerClickType(p, new ClickType.CreateClickType(owner));
        return true;
    }

}
