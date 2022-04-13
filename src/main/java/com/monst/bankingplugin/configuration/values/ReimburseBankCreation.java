package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class ReimburseBankCreation extends BooleanConfigurationValue {

    public ReimburseBankCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-bank-creation", false);
    }

}
