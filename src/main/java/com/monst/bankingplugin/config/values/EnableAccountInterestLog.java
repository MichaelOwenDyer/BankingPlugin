package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableAccountInterestLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableAccountInterestLog(BankingPlugin plugin) {
        super(plugin, "enable-account-interest-log", true);
    }

}
