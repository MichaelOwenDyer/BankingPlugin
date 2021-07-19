package com.monst.bankingplugin.config.values;

public class MaximumBankVolume extends SimpleInteger implements IConfigInteger.Absolute {

    public MaximumBankVolume() {
        super("bank-size-limits.maximum", 100000);
    }

}
