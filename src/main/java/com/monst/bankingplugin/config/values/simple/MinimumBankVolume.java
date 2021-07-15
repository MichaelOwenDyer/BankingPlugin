package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class MinimumBankVolume extends SimpleInteger implements IConfigInteger.Absolute {

    public MinimumBankVolume() {
        super("bank-size-limits.minimum", 125);
    }

}
