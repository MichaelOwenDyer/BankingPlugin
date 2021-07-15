package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class AllowedOfflinePayoutsBeforeReset extends OverridableInteger implements IConfigInteger.Absolute {

    public AllowedOfflinePayoutsBeforeReset() {
        super("allowed-offline-payouts-before-multiplier-reset", 1);
    }

}
