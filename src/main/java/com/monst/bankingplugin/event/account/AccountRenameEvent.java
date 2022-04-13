package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when an {@link Account} is updated and new information.
 */
public class AccountRenameEvent extends SingleAccountEvent {

    private final String newName;

    public AccountRenameEvent(Player player, Account account, String newName) {
        super(player, account);
        this.newName = newName;
    }

    @Override
    public void fire() {
        super.callEvent();
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getNewName() {
        return newName;
    }

}
