package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A non-empty list of positive integers. Defaults to { 1 }
 */
public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("interest-multipliers", Collections.singletonList(1));
    }

    @Override
    public List<Integer> readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        List<Integer> multipliers = new ArrayList<>();
        for (String multiplier : config.getStringList(path))
            try {
                multipliers.add(Math.abs(parseSingle(multiplier)));
            } catch (IntegerParseException ignored) {}
        return multipliers;
    }

    @Override
    public Integer parseSingle(String input) throws IntegerParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IntegerParseException(input);
        }
    }

}
