package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountInfoEvent;
import com.monst.bankingplugin.events.account.AccountPreInfoEvent;
import com.monst.bankingplugin.gui.AccountGui;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountInfo extends AccountCommand.SubCommand {

    AccountInfo() {
        super("info", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_INFO;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to retrieve account info");

        if (args.length > 1) {
            try {
                int id = Integer.parseInt(args[1]);
                Account account = accountUtils.getAccount(id);
                if (account == null) {
                    sender.sendMessage(LangUtils.getMessage(Message.ACCOUNT_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
                    return true;
                }

                plugin.debugf("%s is displaying info for account #%d", sender.getName(), id);

                AccountInfoEvent event = new AccountInfoEvent(sender, account);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    plugin.debug("Account info event cancelled");
                    return true;
                }

                if (sender instanceof Player)
                    new AccountGui(account).open((Player) sender);
                else
                    sender.sendMessage(account.getInformation());
                return true;
            } catch (NumberFormatException ignored) {}
        }

        if (!(sender instanceof Player)) {
            plugin.debug(sender.getName() + " is not a player");
            sender.sendMessage(LangUtils.getMessage(Message.PLAYER_COMMAND_ONLY));
            return true;
        }

        AccountPreInfoEvent event = new AccountPreInfoEvent((Player) sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-info event cancelled");
            return true;
        }

        plugin.debug(sender.getName() + " can now click an account to get info");
        sender.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_INFO));
        ClickType.setPlayerClickType(((Player) sender), ClickType.info());
        return true;
    }

    /**
     * @param player  Player who executed the command and will retrieve the
     *                information
     * @param account Account from which the information will be retrieved
     */
    public static void info(Player player, Account account) {
        plugin.debugf("%s is retrieving %s account info%s (#%d)",
                player.getName(), (account.isOwner(player) ? "their" : account.getOwner().getName() + "'s"),
                (account.isCoowner(player) ? " (is co-owner)" : ""), account.getID());

        AccountInfoEvent event = new AccountInfoEvent(player, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debugf("Account info event cancelled (#%d)", account.getID());
            return;
        }

        new AccountGui(account).open(player);
    }

}
