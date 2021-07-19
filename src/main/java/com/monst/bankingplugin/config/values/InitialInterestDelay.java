package com.monst.bankingplugin.config.values;

public class InitialInterestDelay extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public InitialInterestDelay() {
        super("initial-interest-delay", 0);
    }

}
