package com.monst.bankingplugin.config.values.overridable;

public class MinimumBalance extends OverridableDouble {

    public MinimumBalance() {
        super("minimum-balance", 1000d);
    }

}
