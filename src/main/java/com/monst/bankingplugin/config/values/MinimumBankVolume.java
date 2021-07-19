package com.monst.bankingplugin.config.values;

public class MinimumBankVolume extends SimpleInteger implements IConfigInteger.Absolute {

    public MinimumBankVolume() {
        super("bank-size-limits.minimum", 125);
    }

}
