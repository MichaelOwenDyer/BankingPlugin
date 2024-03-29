package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a double account chest is contracted (one half is broken).
 */
public class AccountContractEvent extends SingleAccountEvent {

    public AccountContractEvent(Player player, Account account) {
        super(player, account);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public void fire() {
        super.callEvent();
    }

}
