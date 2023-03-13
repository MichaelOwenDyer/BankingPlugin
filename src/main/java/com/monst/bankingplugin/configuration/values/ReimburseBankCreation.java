package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class ReimburseBankCreation extends ConfigurationValue<Boolean> {

    public ReimburseBankCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-bank-creation", false, new BooleanTransformer());
    }

}
