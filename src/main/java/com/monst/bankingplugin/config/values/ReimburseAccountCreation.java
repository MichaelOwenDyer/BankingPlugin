package com.monst.bankingplugin.config.values;

public class ReimburseAccountCreation extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public ReimburseAccountCreation() {
        super("reimburse-account-creation", false);
    }

}
