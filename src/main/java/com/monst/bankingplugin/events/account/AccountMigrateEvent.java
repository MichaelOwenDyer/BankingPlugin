package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.Account;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountMigrateEvent extends SingleAccountEvent implements Cancellable {

    private boolean cancelled;
    private final Location newAccountLocation;

    public AccountMigrateEvent(Player player, Account account, Location newAccountLocation) {
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