package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class CountInterestDelayOffline extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public CountInterestDelayOffline(BankingPlugin plugin) {
        super(plugin, "count-interest-delay-offline", false);
    }

}
