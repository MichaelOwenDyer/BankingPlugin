package com.monst.bankingplugin.config.values;

public class InitialInterestDelay extends OverridableInteger implements IConfigInteger.Absolute {

    public InitialInterestDelay() {
        super("initial-interest-delay", 0);
    }

}
