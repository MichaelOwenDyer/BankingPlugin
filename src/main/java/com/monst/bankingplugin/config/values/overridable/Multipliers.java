package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

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
    public List<Integer> readFromFile(MemoryConfiguration config, String path) {
        return Utils.map(super.readFromFile(config, path), Math::abs);
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
