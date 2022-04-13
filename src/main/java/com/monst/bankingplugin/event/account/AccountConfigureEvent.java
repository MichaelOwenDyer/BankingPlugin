package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.command.account.AccountConfigure.AccountField;
import com.monst.bankingplugin.entity.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when an {@link Account} is updated and new information.
 */
public class AccountConfigureEvent extends SingleAccountEvent {

    private final AccountField field;
    private final Object newValue;

    public AccountConfigureEvent(Player player, Account account, AccountField field, Object newValue) {
        super(player, account);
        this.field = field;
        this.newValue = newValue;
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

    public AccountField getField() {
        return field;
    }

    public Object getNewValue() {
        return newValue;
    }

}
