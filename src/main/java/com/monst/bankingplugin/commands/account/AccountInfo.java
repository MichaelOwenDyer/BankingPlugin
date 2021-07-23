package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountInfoCommandEvent;
import com.monst.bankingplugin.events.account.AccountInfoEvent;
import com.monst.bankingplugin.gui.AccountGUI;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountInfo extends SubCommand.AccountSubCommand {

    AccountInfo() {
        super("info", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_INFO;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        PLUGIN.debug(sender.getName() + " wants to retrieve account info");

        if (args.length > 1) {
            try {
                int id = Integer.parseInt(args[1]);
                Account account = accountRepo.getByID(id);
                if (account == null) {
                    sender.sendMessage(Messages.get(Message.ACCOUNT_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
                    return true;
                }

                PLUGIN.debugf("%s is displaying info for account #%d", sender.getName(), id);

                AccountInfoEvent event = new AccountInfoEvent(sender, account);
                event.fire();
                if (event.isCancelled()) {
                    PLUGIN.debug("Account info event cancelled");
                    return true;
                }

                if (sender instanceof Player)
                    new AccountGUI(account).open((Player) sender);
                else
                    sender.sendMessage(account.toConsolePrintout());
                return true;
            } catch (NumberFormatException ignored) {}
        }

        if (!(sender instanceof Player)) {
            PLUGIN.debug(sender.getName() + " is not a player");
            sender.sendMessage(Messages.get(Message.PLAYER_COMMAND_ONLY));
            return true;
        }

        AccountInfoCommandEvent event = new AccountInfoCommandEvent((Player) sender, args);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account info command event cancelled");
            return true;
        }

        PLUGIN.debug(sender.getName() + " can now click an account to get info");
        sender.sendMessage(Messages.get(Message.CLICK_ACCOUNT_INFO));
        ClickType.setInfoClickType((Player) sender);
        return true;
    }

    /**
     * @param player  Player who executed the command and will retrieve the
     *                information
     * @param account Account from which the information will be retrieved
     */
    public static void info(Player player, Account account) {
        ClickType.removeClickType(player);
        PLUGIN.debugf("%s is retrieving %s account info%s (#%d)",
                player.getName(), (account.isOwner(player) ? "their" : account.getOwner().getName() + "'s"),
                (account.isCoOwner(player) ? " (is co-owner)" : ""), account.getID());
        AccountInfoEvent event = new AccountInfoEvent(player, account);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debugf("Account info event cancelled (#%d)", account.getID());
            return;
        }
        new AccountGUI(account).open(player);
    }

}
