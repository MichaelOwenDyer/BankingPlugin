package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class MaximumBankVolume extends SimpleInteger implements IConfigInteger.Absolute {

    public MaximumBankVolume() {
        super("bank-size-limits.maximum", 100000);
    }

}
