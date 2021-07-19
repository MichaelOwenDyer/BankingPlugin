package com.monst.bankingplugin.config.values;

public class AccountCreationPrice extends OverridableDouble implements IConfigDouble.Absolute {

    public AccountCreationPrice() {
        super("account-creation-price", 2500d);
    }

}
