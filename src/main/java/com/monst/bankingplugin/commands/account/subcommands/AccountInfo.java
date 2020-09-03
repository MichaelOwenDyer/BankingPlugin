package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountPreInfoEvent;
import com.monst.bankingplugin.gui.AccountGui;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class AccountInfo extends AccountSubCommand {

    public AccountInfo() {
        super("info", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return Messages.COMMAND_USAGE_ACCOUNT_INFO;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to retrieve account info");

        if (args.length > 1) {
            try {
                int id = Integer.parseInt(args[1]);
                Account account = Objects.requireNonNull(accountUtils.getAccount(id));
                plugin.debug(sender.getName() + " is displaying info for account #" + id);
                if (sender instanceof Player)
                    new AccountGui(account).open((Player) sender);
                else
                    sender.sendMessage(account.getInformation());
                return true;
            } catch (NumberFormatException | NullPointerException ignored) {}
        }

        if (!(sender instanceof Player)) {
            plugin.debug(sender.getName() + " is not a player");
            sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
            return true;
        }

        AccountPreInfoEvent event = new AccountPreInfoEvent((Player) sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-info event cancelled");
            return true;
        }

        plugin.debug(sender.getName() + " can now click an account to get info");
        sender.sendMessage(Messages.CLICK_CHEST_INFO);
        ClickType.setPlayerClickType(((Player) sender), new ClickType(ClickType.EClickType.INFO));
        return true;
    }

}
