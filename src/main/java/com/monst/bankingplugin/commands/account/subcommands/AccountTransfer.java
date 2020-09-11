package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountTransferEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountTransfer extends AccountSubCommand {

    public AccountTransfer() {
        super("transfer", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_TRANSFER) ? Messages.COMMAND_USAGE_ACCOUNT_TRANSFER : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to transfer ownership of an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER)) {
            plugin.debug(p.getName() + " does not have permission to transfer ownership of an account");
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_TRANSFER);
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer newOwner = Utils.getPlayer(args[1]);
        if (newOwner == null) {
            p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
            return false;
        }

        p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "transfer ownership to " + newOwner.getName()));
        ClickType.setPlayerClickType(p, ClickType.transfer(newOwner));
        plugin.debug(p.getName() + " is transferring ownership of an account to " + newOwner.getName());
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();

        List<String> returnCompletions = Utils.getOnlinePlayerNames(plugin);
        if (!sender.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
            returnCompletions.remove(sender.getName());
        return Utils.filter(returnCompletions, string -> string.toLowerCase().startsWith(args[1].toLowerCase()));
    }

    public static void transfer(Player p, OfflinePlayer newOwner, Account account) {
        plugin.debug(p.getName() + " is transferring account #" + account.getID() + " to the ownership of "
                + newOwner.getName());

        AccountTransferEvent event = new AccountTransferEvent(p, account, newOwner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account transfer event cancelled");
            return;
        }

        boolean hasDefaultNickname = account.getRawName().contentEquals(account.getDefaultName());

        p.sendMessage(String.format(Messages.OWNERSHIP_TRANSFERRED, "You",
                Utils.samePlayer(account.getOwner(), p) ? "your account" : p.getName() + "'s account",
                newOwner.getName()));
        account.transferOwnership(newOwner);
        if (hasDefaultNickname)
            account.setName(account.getDefaultName());
        plugin.getAccountUtils().addAccount(account, true);
    }

}
