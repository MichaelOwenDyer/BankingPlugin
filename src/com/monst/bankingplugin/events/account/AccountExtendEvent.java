package com.monst.bankingplugin.events.account;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.monst.bankingplugin.Account;

public class AccountExtendEvent extends AccountEvent implements Cancellable {

	private boolean cancelled;
    private Location newChestLocation;

    public AccountExtendEvent(Player player, Account account, Location newChest) {
        super(player, account);
        this.newChestLocation = newChest;
    }

    /**
     * @return Location of the placed chest
     */
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
