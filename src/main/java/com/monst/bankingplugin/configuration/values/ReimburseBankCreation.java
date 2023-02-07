package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class ReimburseBankCreation extends BooleanConfigurationValue {

    public ReimburseBankCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-bank-creation", false);
    }

}
