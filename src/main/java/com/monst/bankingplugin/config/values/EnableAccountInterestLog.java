package com.monst.bankingplugin.config.values;

public class EnableAccountInterestLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableAccountInterestLog() {
        super("enable-account-interest-log", true);
    }

}
