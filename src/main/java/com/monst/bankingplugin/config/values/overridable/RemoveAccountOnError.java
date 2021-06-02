package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.simple.ConfigBoolean;

public class RemoveAccountOnError extends ConfigBoolean {

    public RemoveAccountOnError() {
        super("remove-account-on-error", false);
    }

}
