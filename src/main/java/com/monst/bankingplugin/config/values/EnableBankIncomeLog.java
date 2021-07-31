package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableBankIncomeLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableBankIncomeLog(BankingPlugin plugin) {
        super(plugin, "enable-bank-income-log", true);
    }

}
