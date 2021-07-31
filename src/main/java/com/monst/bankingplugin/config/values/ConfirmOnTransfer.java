package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ConfirmOnTransfer extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnTransfer(BankingPlugin plugin) {
        super(plugin, "confirm-on-transfer", true);
    }

}
