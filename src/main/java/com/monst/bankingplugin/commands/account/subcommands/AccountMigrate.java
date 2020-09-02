package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountMigrate extends AccountSubCommand {

    public AccountMigrate() {
        super("migrate", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to migrate an account");

        if (!p.hasPermission(Permissions.ACCOUNT_CREATE)) {
            plugin.debug(p.getName() + " does not have permission to migrate an account");
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_MIGRATE);
            return true;
        }

        p.sendMessage(Messages.CLICK_CHEST_MIGRATE_FIRST);
        ClickType.setPlayerClickType(p, new ClickType.MigrateClickType(null));
        plugin.debug(p.getName() + " is migrating an account");
        return true;
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_MIGRATE : "";
    }

}
