package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class RemoveAccountOnError extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public RemoveAccountOnError(BankingPlugin plugin) {
        super(plugin, "remove-account-on-error", false);
    }

}
