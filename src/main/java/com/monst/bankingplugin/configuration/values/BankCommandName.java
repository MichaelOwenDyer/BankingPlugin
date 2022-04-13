package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.impl.StringConfigurationValue;
import com.monst.pluginconfiguration.validation.Bound;
import com.monst.pluginconfiguration.validation.StringValidation;

import java.util.Arrays;
import java.util.List;

/**
 * The name of the bank command.
 */
public class BankCommandName extends StringConfigurationValue {

    public BankCommandName(BankingPlugin plugin) {
        super(plugin, "bank-command-name", "bank");
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
