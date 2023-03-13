package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationBranch;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;

import java.util.Optional;

public class DefaultLimits extends ConfigurationBranch {
    
    /**
     * The default limit to the number of accounts a player may own.
     * This is only used if the player does not have a permission to override it.
     * If this is not set, there is no limit by default.
     */
    public final ConfigurationValue<Optional<Integer>> account;
    
    /**
     * The default limit to the number of banks a player may own.
     * This is only used if the player does not have a permission to override it.
     * If this is not set, there is no limit by default.
     */
    public final ConfigurationValue<Optional<Integer>> bank;
    
    public DefaultLimits(BankingPlugin plugin) {
        super("default-limits");
        this.account = addChild(new ConfigurationValue<>(plugin,
                "account",
                Optional.empty(),
                new IntegerTransformer().absolute().optional()));
        this.bank = addChild(new ConfigurationValue<>(plugin,
                "bank",
                Optional.empty(),
                new IntegerTransformer().absolute().optional()));
    }
    
}
