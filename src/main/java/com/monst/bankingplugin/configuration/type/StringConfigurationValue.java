package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;

/**
 * A configuration value of the type {@link String}.
 */
public abstract class StringConfigurationValue extends ConfigurationValue<String> {

    public StringConfigurationValue(BankingPlugin plugin, String path, String defaultValue) {
        super(plugin, path, defaultValue);
    }
    
    @Override
    protected String parse(String input) {
        return input;
    }

}
