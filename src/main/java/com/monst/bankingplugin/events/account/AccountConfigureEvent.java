package com.monst.bankingplugin.events.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import org.bukkit.entity.Player;

public class AccountConfigureEvent extends SingleAccountEvent {

    private final AccountField field;
    private final String newValue;

    public AccountConfigureEvent(Player player, Account account, AccountField field, String newValue) {
        super(player, account);
        this.field = field;
        this.newValue = newValue;
    }

    public AccountField getField() {
        return field;
    }

    public String getNewValue() {
        return newValue;
    }
}
