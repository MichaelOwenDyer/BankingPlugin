package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;

import javax.annotation.Nonnull;
import java.util.Optional;

class OverriddenDouble extends OverriddenValue<Double> {

    OverriddenDouble(OverridableDouble attribute, Double value) {
        super(attribute, value);
    }

    @Override
    Double parse(@Nonnull String input) throws DoubleParseException {
        return Optional.ofNullable(input)
                .map(i -> Utils.removePunctuation(i, '.'))
                .map(Double::parseDouble)
                .map(Math::abs)
                .map(QuickMath::scale)
                .orElseThrow(() -> new DoubleParseException(input));
    }

}
