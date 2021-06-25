package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a single account chest is extended into a double account chest.
 */
public class AccountExtendEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
    private final AccountLocation newAccountLocation;

    public AccountExtendEvent(@Nonnull Player player, @Nonnull Account account, @Nonnull AccountLocation newAccountLocation) {
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
