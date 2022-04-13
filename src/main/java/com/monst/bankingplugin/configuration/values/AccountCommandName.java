package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.impl.StringConfigurationValue;
import com.monst.pluginconfiguration.validation.Bound;
import com.monst.pluginconfiguration.validation.StringValidation;

import java.util.Arrays;
import java.util.List;

/**
 * The name of the account command.
 */
public class AccountCommandName extends StringConfigurationValue {

    public AccountCommandName(BankingPlugin plugin) {
        super(plugin, "account-command-name", "account");
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
