package com.monst.bankingplugin.config.values;

public class BankCreationPrice extends SimpleDouble implements IConfigDouble.Absolute {

    public BankCreationPrice() {
        super("bank-creation-price", 100000d);
    }

}
