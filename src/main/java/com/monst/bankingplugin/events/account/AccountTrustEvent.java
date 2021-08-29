package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class AccountTrustEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final OfflinePlayer playerToTrust;

    public AccountTrustEvent(CommandSender sender, Account account, OfflinePlayer playerToTrust) {
        super(sender, account);
        this.playerToTrust = playerToTrust;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
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
