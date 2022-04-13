package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class AccountUntrustEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final OfflinePlayer playerToUntrust;

    public AccountUntrustEvent(CommandSender sender, Account account, OfflinePlayer playerToUntrust) {
        super(sender, account);
        this.playerToUntrust = playerToUntrust;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
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
