package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
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

    public static void trust(Player p, Account account, OfflinePlayer playerToTrust) {
        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(p)) {
                p.sendMessage(Messages.MUST_BE_OWNER);
                return;
            }
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRUST_OTHER);
            return;
        }

        boolean isSelf = Utils.samePlayer(playerToTrust, p);
        if (account.isTrusted(playerToTrust)) {
            plugin.debugf("%s was already trusted on that account (#%d)", playerToTrust.getName(), account.getID());
            p.sendMessage(String.format(account.isOwner(playerToTrust) ? Messages.ALREADY_OWNER : Messages.ALREADY_COOWNER,
                    isSelf ? "You are" : playerToTrust.getName() + " is", "account"));
            return;
        }

        plugin.debugf("%s has trusted %s to %s account (#%d)", p.getName(), playerToTrust.getName(),
                (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        p.sendMessage(String.format(Messages.ADDED_COOWNER, isSelf ? "You were" : playerToTrust.getName() + " was"));
        account.trustPlayer(playerToTrust);
    }

}
