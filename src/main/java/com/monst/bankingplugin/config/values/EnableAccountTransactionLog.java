package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableAccountTransactionLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableAccountTransactionLog(BankingPlugin plugin) {
        super(plugin, "enable-account-transaction-log", true);
    }

}
