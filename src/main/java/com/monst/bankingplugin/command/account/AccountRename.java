package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountRenameEvent;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.entity.Player;

public class AccountRename extends PlayerSubCommand {

    AccountRename(BankingPlugin plugin) {
		super(plugin, "rename");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_RENAME;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_RENAME;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_RENAME;
    }

    @Override
    protected void execute(Player player, String[] args) throws ExecutionException {
        String nickname = String.join(" ", args).trim();
        if (!nickname.isEmpty() && plugin.config().nameRegex.doesNotMatch(nickname))
            throw new ExecutionException(plugin, Message.NAME_NOT_ALLOWED
                    .with(Placeholder.NAME).as(nickname)
                    .and(Placeholder.PATTERN).as(plugin.config().nameRegex.get()));

        ClickAction.setAccountClickAction(player, account -> rename(player, account, nickname));
        player.sendMessage(Message.CLICK_ACCOUNT_RENAME.translate(plugin));
    }

    private void rename(Player executor, Account account, String newName) throws ExecutionException {
        ClickAction.remove(executor);
        if (!account.isTrusted(executor) && Permission.ACCOUNT_RENAME_OTHER.notOwnedBy(executor))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_RENAME_OTHER);

        if (newName.isEmpty()) {
            plugin.debugf("%s has reset nickname of account #%d", executor.getName(), account.getID());
            account.setCustomName(null);
            newName = account.getName();
        } else {
            plugin.debugf("%s has renamed account #%d to \"%s\"", executor.getName(), account.getID(), newName);
            account.setCustomName(newName);
        }
        executor.sendMessage(Message.ACCOUNT_RENAMED.with(Placeholder.NAME).as(newName).translate(plugin));
        new AccountRenameEvent(executor, account, newName).fire();
        plugin.getAccountService().update(account);
    }

}
