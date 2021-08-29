package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
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
