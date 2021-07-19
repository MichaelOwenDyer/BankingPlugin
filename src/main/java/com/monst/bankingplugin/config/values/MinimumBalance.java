package com.monst.bankingplugin.config.values;

public class MinimumBalance extends OverridableDouble implements IConfigDouble.Absolute {

    public MinimumBalance() {
        super("minimum-account-balance", 1000d);
    }

}
