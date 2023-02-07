package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountInfoCommandEvent;
import com.monst.bankingplugin.event.account.AccountInfoEvent;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.gui.AccountGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.command.ClickAction;
import org.bukkit.entity.Player;

public class AccountInfo extends PlayerSubCommand {

    AccountInfo(BankingPlugin plugin) {
		super(plugin, "info");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_INFO;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException {
        new AccountInfoCommandEvent(player, args).fire();

        plugin.debugf("%s can now click an account to see the GUI", player.getName());
        player.sendMessage(Message.CLICK_ACCOUNT_INFO.translate(plugin));
        ClickAction.setAccountClickAction(player, account -> info(player, account));
    }

    private void info(Player player, Account account) throws EventCancelledException {
        ClickAction.remove(player);
        plugin.debugf("%s is viewing the GUI of account #%d", player.getName(), account.getID());
        new AccountInfoEvent(player, account).fire();
        new AccountGUI(plugin, player, account).open();
    }

}
