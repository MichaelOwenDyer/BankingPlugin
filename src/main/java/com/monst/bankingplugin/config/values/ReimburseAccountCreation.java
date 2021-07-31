package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ReimburseAccountCreation extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public ReimburseAccountCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-account-creation", false);
    }

}
