package com.monst.bankingplugin.config.values;

public class EnableAccountTransactionLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableAccountTransactionLog() {
        super("enable-account-transaction-log", true);
    }

}
