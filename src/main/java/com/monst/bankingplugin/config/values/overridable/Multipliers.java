package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * A non-empty list of positive integers. Defaults to { 1 }
 */
public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("multipliers", Collections.singletonList(1));
    }

    @Override
    public List<Integer> readValueFromFile(MemoryConfiguration config, String path) {
        List<Integer> multipliers = config.getIntegerList(path);
        return multipliers.isEmpty() ? null : multipliers;
    }

    @Override
    public Integer parseSingle(String input) {
        return Utils.parseInteger(input);
    }

}
