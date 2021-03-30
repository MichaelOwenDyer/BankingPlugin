package com.monst.bankingplugin.config.values.overridable;

import javax.annotation.Nonnull;

class OverriddenBoolean extends OverriddenValue<Boolean> {

    OverriddenBoolean(OverridableBoolean attribute, Boolean value) {
        super(attribute, value);
    }

    @Override
    Boolean parse(@Nonnull String input) {
        return Boolean.parseBoolean(input);
    }

}
