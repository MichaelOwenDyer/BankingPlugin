package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

public abstract class IntegerConfigurationValue extends com.monst.pluginconfiguration.impl.IntegerConfigurationValue {

    final BankingPlugin plugin;

    public IntegerConfigurationValue(BankingPlugin plugin, String path, Integer defaultValue) {
        super(plugin, path, defaultValue);
        this.plugin = plugin;
    }

    @Override
    protected ArgumentParseException createArgumentParseException(String input) {
        return new ArgumentParseException(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(input).translate(plugin));
    }

}
