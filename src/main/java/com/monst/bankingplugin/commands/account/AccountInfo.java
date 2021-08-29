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
		super(plugin, "info", true);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_INFO;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if (args.length > 0) {
            try {
                int id = Integer.parseInt(args[0]);
                Account account = plugin.getAccountRepository().getByID(id);
                if (account == null) {
                    p.sendMessage(Message.ACCOUNT_NOT_FOUND.with(Placeholder.INPUT).as(args[0]).translate());
                    return true;
                }

                plugin.debugf("%s is viewing the GUI of account #%d", p.getName(), id);

                AccountInfoEvent event = new AccountInfoEvent(p, account);
                event.fire();
                if (event.isCancelled()) {
                    plugin.debug("Account info event cancelled");
                    return true;
                }

                new AccountGUI(account).open(p);
                return true;
            } catch (NumberFormatException ignored) {}
        }

        AccountInfoCommandEvent event = new AccountInfoCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account info command event cancelled");
            return true;
        }

        plugin.debugf("%s can now click an account to see the GUI", p.getName());
        p.sendMessage(Message.CLICK_ACCOUNT_INFO.translate());
        ClickType.setInfoClickType(p);
        return true;
    }

    public static void info(BankingPlugin plugin, Player p, Account account) {
        plugin.debugf("%s is viewing the GUI of account #%d", p.getName(), account.getID());
        AccountInfoEvent event = new AccountInfoEvent(p, account);
        event.fire();
        if (event.isCancelled()) {
            plugin.debugf("Account info event cancelled at account #%d)", account.getID());
            return;
        }
        new AccountGUI(account).open(p);
    }

}
