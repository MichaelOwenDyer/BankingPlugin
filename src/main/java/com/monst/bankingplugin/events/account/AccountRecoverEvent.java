package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.ClickType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * This event is fired when a player clicks a chest and a
 * {@link ClickType} of type <b>recover</b>.
 */
public class AccountRecoverEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final AccountLocation newAccountLocation;

    public AccountRecoverEvent(Player player, Account account, AccountLocation newAccountLocation) {
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
