package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationBranch;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class Integrations extends ConfigurationBranch {
    
    /**
     * Whether to enable GriefPrevention integration.
     */
    public final ConfigurationValue<Boolean> griefPrevention;
    
    /**
     * Whether to enable WorldGuard integration.
     */
    public final ConfigurationValue<Boolean> worldGuard;
    
    /**
     * Whether to enable WorldEdit integration.
     */
    public final ConfigurationValue<Boolean> worldEdit;
    
    public Integrations(BankingPlugin plugin) {
        super("integrations");
        this.griefPrevention = addChild(
                new ConfigurationValue<>(plugin, "griefprevention", true, new BooleanTransformer()));
        this.worldGuard = addChild(
                new ConfigurationValue<>(plugin, "worldguard", true, new BooleanTransformer()));
        this.worldEdit = addChild(
                new ConfigurationValue<>(plugin, "worldedit", true, new BooleanTransformer()));
    }
    
}
