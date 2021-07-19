package com.monst.bankingplugin.config.values;

public class AllowedOfflinePayouts extends OverridableInteger implements IConfigInteger.Absolute {

    public AllowedOfflinePayouts() {
        super("allowed-offline-payouts", 1);
    }

}
