package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This account is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountMigrate} command.
 */
public class AccountPreMigrateEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;

    public AccountPreMigrateEvent(@Nonnull Player player, @Nonnull Account account) {
        super(player, account);
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
