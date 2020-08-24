package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AccountExtendEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;
    private final Location newChestLocation;

    public AccountExtendEvent(Player player, Account account, Location newChest) {
        super(player, account);
        this.newChestLocation = newChest;
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
