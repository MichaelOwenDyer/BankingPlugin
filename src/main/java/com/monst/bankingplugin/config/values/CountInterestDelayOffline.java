package com.monst.bankingplugin.config.values;

public class CountInterestDelayOffline extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public CountInterestDelayOffline() {
        super("count-interest-delay-offline", false);
    }

}
