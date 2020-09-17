package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * This event is fired when a player clicks a chest with a
 * {@link com.monst.bankingplugin.utils.ClickType.RecoverClickType}.
 */
public class AccountRecoverEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final Location newAccountLocation;

    public AccountRecoverEvent(Player player, Account account, Location newAccountLocation) {
        super(player, account);
        this.newAccountLocation = newAccountLocation;
    }

    public Location getNewAccountLocation() {
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
