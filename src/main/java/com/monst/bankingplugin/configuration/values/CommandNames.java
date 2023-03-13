package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationBranch;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.StringTransformer;

import static com.monst.bankingplugin.configuration.transform.StringTransformer.lowercase;
import static com.monst.bankingplugin.configuration.transform.StringTransformer.noSpaces;

public class CommandNames extends ConfigurationBranch {
    
    /**
     * The name of the account command.
     */
    public final ConfigurationValue<String> account;
    
    /**
     * The name of the bank command.
     */
    public final ConfigurationValue<String> bank;
    
    /**
     * The name of the plugin command.
     */
    public final ConfigurationValue<String> plugin;
    
    public CommandNames(BankingPlugin plugin) {
        super("command-names");
        this.account = addChild(new ConfigurationValue<>(plugin, "account", "account",
                new StringTransformer().bounded(lowercase().and(noSpaces()))));
        this.bank = addChild(new ConfigurationValue<>(plugin, "bank", "bank",
                new StringTransformer().bounded(lowercase().and(noSpaces()))));
        this.plugin = addChild(new ConfigurationValue<>(plugin, "plugin", "bp",
                new StringTransformer().bounded(lowercase().and(noSpaces()))));
    }
    
}
