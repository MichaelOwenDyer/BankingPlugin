package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class AllowedOfflinePayouts extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public AllowedOfflinePayouts(BankingPlugin plugin) {
        super(plugin, "allowed-offline-payouts", 1);
    }

}
