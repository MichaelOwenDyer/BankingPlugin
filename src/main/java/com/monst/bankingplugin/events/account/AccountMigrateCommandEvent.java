package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This account is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountMigrate} command.
 */
public class AccountMigrateCommandEvent extends AccountCommandEvent implements Cancellable {

    private boolean cancelled;

    public AccountMigrateCommandEvent(Player player, String[] args) {
        super(player, args);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
