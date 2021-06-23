package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a single account chest is extended into a double account chest.
 */
public class AccountExtendEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
    private final ChestLocation newChestLocation;

    public AccountExtendEvent(@Nonnull Player player, @Nonnull Account account, @Nonnull ChestLocation newChestLocation) {
        super(player, account);
        this.newChestLocation = newChestLocation;
    }

    public ChestLocation getNewChestLocation() {
        return newChestLocation;
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
