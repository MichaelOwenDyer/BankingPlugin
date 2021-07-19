package com.monst.bankingplugin.config.values;

public class AllowSelfBanking extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public AllowSelfBanking() {
        super("allow-self-banking", false);
    }

}
