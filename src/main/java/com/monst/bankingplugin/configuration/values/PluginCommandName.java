package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.impl.StringConfigurationValue;
import com.monst.pluginconfiguration.validation.Bound;
import com.monst.pluginconfiguration.validation.StringValidation;

import java.util.Arrays;
import java.util.List;

public class PluginCommandName extends StringConfigurationValue {

    public PluginCommandName(BankingPlugin plugin) {
        super(plugin, "plugin-command-name", "bp");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

    @Override
    protected List<Bound<String>> getBounds() {
        return Arrays.asList(
                StringValidation.lowercase(),
                StringValidation.noSpaces()
        );
    }

}
