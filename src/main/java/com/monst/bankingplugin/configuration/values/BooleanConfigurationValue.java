package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

public abstract class BooleanConfigurationValue extends com.monst.pluginconfiguration.impl.BooleanConfigurationValue {

    final BankingPlugin plugin;

    public BooleanConfigurationValue(BankingPlugin plugin, String path, Boolean defaultValue) {
        super(plugin, path, defaultValue);
        this.plugin = plugin;
    }

    @Override
    protected ArgumentParseException createArgumentParseException(String input) {
        return new ArgumentParseException(Message.NOT_A_BOOLEAN.with(Placeholder.INPUT).as(input).translate(plugin));
    }

}
