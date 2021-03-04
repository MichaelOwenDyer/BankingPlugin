package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * This event is fired when a player clicks a chest with a
 * {@link com.monst.bankingplugin.utils.ClickType} of type <b>recover</b>.
 */
public class AccountRecoverEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final ChestLocation newAccountLocation;

    public AccountRecoverEvent(Player player, Account account, ChestLocation newAccountLocation) {
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
