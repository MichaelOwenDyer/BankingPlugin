package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountTransferCommandEvent;
import com.monst.bankingplugin.events.account.AccountTransferEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountTransfer extends AccountCommand.SubCommand implements ConfirmableAccountAction {

    private static AccountTransfer instance;

    public static AccountTransfer getInstance() {
        return instance;
    }

    AccountTransfer() {
        super("transfer", true);
        instance = this;
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_TRANSFER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_TRANSFER;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to transfer ownership of an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER)) {
            plugin.debug(p.getName() + " does not have permission to transfer ownership of an account");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRANSFER));
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer newOwner = Utils.getPlayer(args[1]);
        if (newOwner == null) {
            p.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }

        AccountTransferCommandEvent event = new AccountTransferCommandEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-transfer event cancelled");
            return true;
        }

        p.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_TRANSFER, new Replacement(Placeholder.PLAYER, newOwner::getName)));
        ClickType.setPlayerClickType(p, ClickType.transfer(newOwner));
        plugin.debug(p.getName() + " is transferring ownership of an account to " + newOwner.getName());
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();

        List<String> returnCompletions = Utils.getOnlinePlayerNames(plugin);
        if (!sender.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
            returnCompletions.remove(sender.getName());
        return Utils.filter(returnCompletions, string -> string.toLowerCase().startsWith(args[1].toLowerCase()));
    }

    public void transfer(Player p, OfflinePlayer newOwner, Account account) {
        plugin.debug(p.getName() + " is transferring account #" + account.getID() + " to the ownership of " + newOwner.getName());

        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER)) {
            plugin.debug(p.getName() + " does not have permission to transfer the account.");
            if (account.isTrusted(p))
                p.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
            else
                p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRANSFER_OTHER));
            ClickType.removePlayerClickType(p);
            return;
        }

        if (account.isOwner(newOwner)) {
            plugin.debug(p.getName() + " is already owner of account");
            p.sendMessage(LangUtils.getMessage(Message.ALREADY_OWNER, new Replacement(Placeholder.PLAYER, newOwner::getName)));
            ClickType.removePlayerClickType(p);
            return;
        }

        if (Config.confirmOnTransfer && !isConfirmed(p, account.getID())) {
            plugin.debug("Needs confirmation");
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CONFIRM_TRANSFER,
                    new Replacement(Placeholder.PLAYER, newOwner::getName)
            ));
            return;
        }

        AccountTransferEvent event = new AccountTransferEvent(p, account, newOwner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account transfer event cancelled");
            return;
        }

        boolean hasCustomName = account.hasCustomName();

        MailingRoom mailingRoom = new MailingRoom(LangUtils.getMessage(Message.ACCOUNT_TRANSFERRED,
                new Replacement(Placeholder.PLAYER, newOwner::getName)
        ));
        mailingRoom.addRecipient(p);
        mailingRoom.send();
        mailingRoom.newMessage(LangUtils.getMessage(Message.ACCOUNT_TRANSFERRED_TO_YOU,
                new Replacement(Placeholder.PLAYER, p::getName)
        ));
        mailingRoom.addOfflineRecipient(newOwner);
        mailingRoom.removeRecipient(p);
        mailingRoom.send();

        account.setOwner(newOwner);
        if (!hasCustomName)
            account.resetName();
        plugin.getAccountRepository().update(account, null, AccountField.OWNER);
        ClickType.removePlayerClickType(p);
    }

}
