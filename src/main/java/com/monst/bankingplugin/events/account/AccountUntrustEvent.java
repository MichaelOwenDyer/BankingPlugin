package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

public class AccountUntrustEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private OfflinePlayer playerToUntrust;

    public AccountUntrustEvent(@Nonnull CommandSender sender, @Nonnull Account account, @Nonnull OfflinePlayer playerToUntrust) {
        super(sender, account);
        this.playerToUntrust = playerToUntrust;
    }

    public OfflinePlayer getPlayerToUntrust() {
        return playerToUntrust;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
