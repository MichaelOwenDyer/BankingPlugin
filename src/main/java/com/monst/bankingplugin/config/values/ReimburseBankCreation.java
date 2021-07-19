package com.monst.bankingplugin.config.values;

public class ReimburseBankCreation extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ReimburseBankCreation() {
        super("reimburse-bank-creation", false);
    }

}
