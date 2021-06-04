package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.simple.SimpleBoolean;

public class RemoveAccountOnError extends SimpleBoolean {

    public RemoveAccountOnError() {
        super("remove-account-on-error", false);
    }

}
