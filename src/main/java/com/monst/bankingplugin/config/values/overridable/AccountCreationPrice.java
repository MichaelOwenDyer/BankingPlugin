package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigDouble;

public class AccountCreationPrice extends OverridableDouble implements IConfigDouble.Absolute {

    public AccountCreationPrice() {
        super("account-creation-price", 2500d);
    }

}
