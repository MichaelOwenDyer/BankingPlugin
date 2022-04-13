package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

/**
 * Configuration value that determines whether players can create accounts at their own bank.
 */
public class AllowSelfBanking extends BooleanConfigurationValue {

    public AllowSelfBanking(BankingPlugin plugin) {
        super(plugin, "allow-self-banking", false);
    }

}
