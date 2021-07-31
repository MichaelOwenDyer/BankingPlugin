package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ConfirmOnRemove extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnRemove(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove", true);
    }

}
