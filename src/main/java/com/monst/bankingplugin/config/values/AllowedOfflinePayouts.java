package com.monst.bankingplugin.config.values;

public class AllowedOfflinePayouts extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public AllowedOfflinePayouts() {
        super("allowed-offline-payouts", 1);
    }

}
