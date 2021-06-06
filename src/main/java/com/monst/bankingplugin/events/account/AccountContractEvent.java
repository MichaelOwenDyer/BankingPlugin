package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import javax.annotation.Nonnull;

/**
 * This event is fired when a double account chest is contracted (one half is broken).
 */
public class AccountContractEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

    public AccountContractEvent(@Nonnull Player player, @Nonnull Account account) {
        super(player, account);
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
