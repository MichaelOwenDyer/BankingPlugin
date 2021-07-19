package com.monst.bankingplugin.config.values;

public class EnableBankIncomeLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableBankIncomeLog() {
        super("enable-bank-income-log", true);
    }

}
