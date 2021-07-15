package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class InitialInterestDelay extends OverridableInteger implements IConfigInteger.Absolute {

    public InitialInterestDelay() {
        super("initial-interest-delay", 0);
    }

}
