package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This account is fired when a player with a {@link com.monst.bankingplugin.utils.ClickType} of type <b>migrate</b>
 * clicks an empty chest to migrate an account to.
 */
public class AccountMigrateEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final ChestLocation newAccountLocation;

    public AccountMigrateEvent(@Nonnull Player player, @Nonnull Account account, @Nonnull ChestLocation newAccountLocation) {
        super(player, account);
        this.newAccountLocation = newAccountLocation;
    }

    public ChestLocation getNewAccountLocation() {
        return newAccountLocation;
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
