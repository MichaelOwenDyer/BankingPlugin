package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

/**
 * Configuration value that determines whether players can create accounts at their own bank.
 */
public class AllowSelfBanking extends ConfigurationValue<Boolean> {

    public AllowSelfBanking(BankingPlugin plugin) {
        super(plugin, "allow-self-banking", false, new BooleanTransformer());
    }
    
}
