package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

public class AccountTrustEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private OfflinePlayer playerToTrust;

    public AccountTrustEvent(@Nonnull CommandSender sender, @Nonnull Account account, @Nonnull OfflinePlayer playerToTrust) {
        super(sender, account);
        this.playerToTrust = playerToTrust;
    }

    public OfflinePlayer getPlayerToTrust() {
        return playerToTrust;
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
