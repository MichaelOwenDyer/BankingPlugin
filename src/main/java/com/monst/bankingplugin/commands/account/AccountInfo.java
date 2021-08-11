package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountInfoCommandEvent;
import com.monst.bankingplugin.events.account.AccountInfoEvent;
import com.monst.bankingplugin.gui.AccountGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountInfo extends SubCommand {

    AccountInfo(BankingPlugin plugin) {
		super(plugin, "info", false);
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
                Account account = plugin.getAccountRepository().getByID(id);
                if (account == null) {
                    sender.sendMessage(Message.ACCOUNT_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
                    return true;
                }

                plugin.debugf("%s is displaying info for account #%d", sender.getName(), id);

                AccountInfoEvent event = new AccountInfoEvent(sender, account);
                event.fire();
                if (event.isCancelled()) {
                    plugin.debug("Account info event cancelled");
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
            plugin.debug(sender.getName() + " is not a player");
            sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate());
            return true;
        }

        AccountInfoCommandEvent event = new AccountInfoCommandEvent((Player) sender, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account info command event cancelled");
            return true;
        }

        plugin.debug(sender.getName() + " can now click an account to get info");
        sender.sendMessage(Message.CLICK_ACCOUNT_INFO.translate());
        ClickType.setInfoClickType((Player) sender);
        return true;
    }

    public static void info(BankingPlugin plugin, Player p, Account account) {
        plugin.debugf("%s is viewing account info (#%d)", p.getName(), account.getID());
        AccountInfoEvent event = new AccountInfoEvent(p, account);
        event.fire();
        if (event.isCancelled()) {
            plugin.debugf("Account info event cancelled (#%d)", account.getID());
            return;
        }
        new AccountGUI(account).open(p);
    }

}
