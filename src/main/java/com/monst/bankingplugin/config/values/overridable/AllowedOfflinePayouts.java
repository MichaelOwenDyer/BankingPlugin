package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class AllowedOfflinePayouts extends OverridableInteger implements IConfigInteger.Absolute {

    public AllowedOfflinePayouts() {
        super("allowed-offline-payouts", 1);
    }

}
