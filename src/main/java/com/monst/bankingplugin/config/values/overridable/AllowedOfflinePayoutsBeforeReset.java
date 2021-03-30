package com.monst.bankingplugin.config.values.overridable;

public class AllowedOfflinePayoutsBeforeReset extends OverridableInteger {

    public AllowedOfflinePayoutsBeforeReset() {
        super("allowed-offline-payouts-before-reset", 1);
    }

}
