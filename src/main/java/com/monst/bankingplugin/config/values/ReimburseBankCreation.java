package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ReimburseBankCreation extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ReimburseBankCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-bank-creation", false);
    }

}
