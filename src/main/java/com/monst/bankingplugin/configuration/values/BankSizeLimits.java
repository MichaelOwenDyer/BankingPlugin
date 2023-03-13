package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationBranch;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.LongTransformer;

import java.util.Optional;

public class BankSizeLimits extends ConfigurationBranch {
    
    public final ConfigurationValue<Optional<Long>> maximum;
    public final ConfigurationValue<Optional<Long>> minimum;
    
    public BankSizeLimits(BankingPlugin plugin) {
        super("bank-size-limits");
        this.maximum = addChild(new ConfigurationValue<>(plugin,
                "maximum",
                Optional.empty(),
                new LongTransformer().absolute().optional()));
        this.minimum = addChild(new ConfigurationValue<>(plugin,
                "minimum",
                Optional.empty(),
                new LongTransformer().absolute().optional()));
    }
    
}
