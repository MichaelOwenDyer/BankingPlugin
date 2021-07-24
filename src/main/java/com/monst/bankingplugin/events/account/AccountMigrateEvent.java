package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This account is fired when a player with a {@link ClickType} of type <b>migrate</b>
 * clicks an empty chest to migrate an account to.
 */
public class AccountMigrateEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final AccountLocation newAccountLocation;

    public AccountMigrateEvent(@Nonnull Player player, @Nonnull Account account, @Nonnull AccountLocation newAccountLocation) {
        super(player, account);
        this.newAccountLocation = newAccountLocation;
    }

    public AccountLocation getNewAccountLocation() {
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
