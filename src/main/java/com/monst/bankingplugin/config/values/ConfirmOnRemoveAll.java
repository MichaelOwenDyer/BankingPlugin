package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ConfirmOnRemoveAll extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnRemoveAll(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove-all", true);
    }

}
