package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountTransferCommandEvent;
import com.monst.bankingplugin.events.account.AccountTransferEvent;
import com.monst.bankingplugin.lang.MailingRoom;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountTransfer extends SubCommand.AccountSubCommand {

    AccountTransfer(BankingPlugin plugin) {
		super(plugin, "transfer", true);
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
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_TRANSFER.translate());
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer newOwner = Utils.getPlayer(args[1]);
        if (newOwner == null) {
            p.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }

        AccountTransferCommandEvent event = new AccountTransferCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account transfer command event cancelled");
            return true;
        }

        p.sendMessage(Message.CLICK_ACCOUNT_TRANSFER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
        ClickType.setTransferClickType(p, newOwner);
        plugin.debug(p.getName() + " is transferring ownership of an account to " + newOwner.getName());
        return true;
    }

    public static void transfer(BankingPlugin plugin, Player p, Account account, OfflinePlayer newOwner, boolean confirmed) {
        plugin.debug(p.getName() + " is transferring account #" + account.getID() + " to the ownership of " + newOwner.getName());

        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER)) {
            plugin.debug(p.getName() + " does not have permission to transfer the account.");
            Message message = account.isTrusted(p) ? Message.MUST_BE_OWNER : Message.NO_PERMISSION_ACCOUNT_TRANSFER_OTHER;
            p.sendMessage(message.translate());
            ClickType.removeClickType(p);
            return;
        }

        if (account.isOwner(newOwner)) {
            plugin.debug(p.getName() + " is already owner of account");
            p.sendMessage(Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
            ClickType.removeClickType(p);
            return;
        }

        if (Config.confirmOnTransfer.get() && !confirmed) {
            plugin.debug("Needs confirmation");
            p.sendMessage(Message.ACCOUNT_CONFIRM_TRANSFER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
            ClickType.confirmClickType(p);
            return;
        }

        AccountTransferEvent event = new AccountTransferEvent(p, account, newOwner);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account transfer event cancelled");
            return;
        }

        boolean hasCustomName = account.hasCustomName();

        MailingRoom mailingRoom = new MailingRoom(Message.ACCOUNT_TRANSFERRED
                .with(Placeholder.PLAYER).as(newOwner.getName())
                .translate());
        mailingRoom.addRecipient(p);
        mailingRoom.send();
        mailingRoom.newMessage(Message.ACCOUNT_TRANSFERRED_TO_YOU
                .with(Placeholder.PLAYER).as(p.getName())
                .translate());
        mailingRoom.addOfflineRecipient(newOwner);
        mailingRoom.removeRecipient(p);
        mailingRoom.send();

        account.setOwner(newOwner);
        if (!hasCustomName)
            account.resetName();
        plugin.getAccountRepository().update(account, AccountField.OWNER);
        ClickType.removeClickType(p);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        List<String> returnCompletions = Utils.getOnlinePlayerNames();
        if (!player.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
            returnCompletions.remove(player.getName());
        return Utils.filter(returnCompletions, string -> Utils.startsWithIgnoreCase(string, args[0]));
    }

}
