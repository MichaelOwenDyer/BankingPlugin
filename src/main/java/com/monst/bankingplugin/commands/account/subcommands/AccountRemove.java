package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.events.account.AccountPreRemoveEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRemove extends AccountSubCommand {

    public AccountRemove() {
        super("remove", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_REMOVE : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to remove an account");

        AccountPreRemoveEvent event = new AccountPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-remove event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to remove an account");
        p.sendMessage(Messages.CLICK_CHEST_REMOVE);
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.REMOVE));
        return true;
    }

}
