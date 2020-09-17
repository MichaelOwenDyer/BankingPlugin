package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a single account chest is extended into a double account chest.
 */
public class AccountExtendEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
    private final Location newChestLocation;

    public AccountExtendEvent(@Nonnull Player player, @Nonnull Account account, @Nonnull Location newChestLocation) {
        super(player, account);
        this.newChestLocation = newChestLocation;
    }

    public Location getNewChestLocation() {
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
