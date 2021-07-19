package com.monst.bankingplugin.config.values;

public class EnableMail extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableMail() {
        super("enable-mail", false);
    }

}
