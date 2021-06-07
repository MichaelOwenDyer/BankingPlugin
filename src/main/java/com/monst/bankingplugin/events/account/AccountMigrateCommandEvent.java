package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This account is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountMigrate} command.
 */
public class AccountMigrateCommandEvent extends AccountCommandEvent implements Cancellable {

    private boolean cancelled;

    public AccountMigrateCommandEvent(@Nonnull Player player, @Nonnull String[] args) {
        super(player, args);
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
