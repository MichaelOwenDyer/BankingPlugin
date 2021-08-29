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
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountTransfer extends SubCommand {

    AccountTransfer(BankingPlugin plugin) {
		super(plugin, "transfer", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_TRANSFER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_TRANSFER;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_TRANSFER;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1)
            return false;

        Player p = (Player) sender;
        OfflinePlayer newOwner = Utils.getPlayer(args[0]);
        if (newOwner == null) {
            p.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[0]).translate());
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
        plugin.debugf("%s is transferring ownership of an account to %s", p.getName(), newOwner.getName());
        return true;
    }

    public static void transfer(BankingPlugin plugin, Player player, Account account, OfflinePlayer newOwner) {
        plugin.debugf("%s is transferring ownership of account #%d to %s", player.getName(), account.getID(), newOwner.getName());

        if (!account.isOwner(player) && Permission.ACCOUNT_TRANSFER_OTHER.notOwnedBy(player)) {
            plugin.debugf("%s does not have permission to transfer the account.", player.getName());
            Message message = account.isTrusted(player) ? Message.MUST_BE_OWNER : Message.NO_PERMISSION_ACCOUNT_TRANSFER_OTHER;
            player.sendMessage(message.translate());
            ClickType.removeClickType(player);
            return;
        }

        if (account.isOwner(newOwner)) {
            plugin.debugf("%s is already owner of account", player.getName());
            player.sendMessage(Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
            ClickType.removeClickType(player);
            return;
        }

        if (Config.confirmOnTransfer.get() && ClickType.needsConfirmation(player)) {
            plugin.debug("Account transfer needs confirmation");
            player.sendMessage(Message.ACCOUNT_CONFIRM_TRANSFER.with(Placeholder.PLAYER).as(newOwner.getName()).translate());
            ClickType.confirmClickType(player);
            return;
        }

        AccountTransferEvent event = new AccountTransferEvent(player, account, newOwner);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account transfer event cancelled");
            return;
        }

        boolean hasCustomName = account.hasCustomName();

        String message = Message.ACCOUNT_TRANSFERRED.with(Placeholder.PLAYER).as(newOwner.getName()).translate();
        MailingRoom.draft(message).to(player).send();
        message = Message.ACCOUNT_TRANSFERRED_TO_YOU.with(Placeholder.PLAYER).as(player.getName()).translate();
        MailingRoom.draft(message).to(newOwner).butNotTo(player).send();

        account.setOwner(newOwner);
        if (!hasCustomName)
            account.resetName();
        plugin.getAccountRepository().update(account, AccountField.OWNER);
        ClickType.removeClickType(player);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        List<String> returnCompletions = Utils.getOnlinePlayerNames();
        if (Permission.ACCOUNT_TRANSFER_OTHER.notOwnedBy(player))
            returnCompletions.remove(player.getName());
        return Utils.filter(returnCompletions, string -> Utils.startsWithIgnoreCase(string, args[0]));
    }

}
