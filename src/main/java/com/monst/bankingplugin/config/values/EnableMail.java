package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableMail extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableMail(BankingPlugin plugin) {
        super(plugin, "enable-mail", false);
    }

}
