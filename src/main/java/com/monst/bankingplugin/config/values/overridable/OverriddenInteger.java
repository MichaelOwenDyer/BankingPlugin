package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.IntegerParseException;

import javax.annotation.Nonnull;
import java.util.Optional;

class OverriddenInteger extends OverriddenValue<Integer> {

    OverriddenInteger(OverridableInteger attribute, Integer value) {
        super(attribute, value);
    }

    @Override
    Integer parse(@Nonnull String input) throws IntegerParseException {
        return Optional.ofNullable(input)
                .map(Integer::parseInt)
                .map(Math::abs)
                .orElseThrow(() -> new IntegerParseException(input));
    }

}
