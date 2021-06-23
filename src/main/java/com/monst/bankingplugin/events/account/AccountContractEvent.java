package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * This event is fired when a double account chest is contracted (one half is broken).
 */
public class AccountContractEvent extends SingleAccountEvent {

    public AccountContractEvent(@Nonnull Player player, @Nonnull Account account) {
        super(player, account);
    }

}
