package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class OverridableDouble extends OverridableValue<Double> {

    OverridableDouble(String path, Double defaultValue) {
        super(path, defaultValue, MemoryConfiguration::getDouble);
    }

    @Override
    public Double parse(@Nonnull String input) throws DoubleParseException {
        return Optional.ofNullable(input)
                .map(i -> Utils.removePunctuation(i, '.'))
                .map(Utils::parseDouble)
                .map(Math::abs)
                .map(QuickMath::scale)
                .orElseThrow(() -> new DoubleParseException(input));
    }

}
