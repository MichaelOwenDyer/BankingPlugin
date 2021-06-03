package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A non-empty list of positive integers. Defaults to { 1 }
 */
public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("interest-multipliers", Collections.singletonList(1));
    }

    @Override
    public List<Integer> readValueFromFile(MemoryConfiguration config, String path) {
        List<Integer> multipliers = config.getIntegerList(path).stream().map(Math::abs).collect(Collectors.toList());
        return multipliers.isEmpty() ? null : multipliers;
    }

    @Override
    public Integer parseSingle(String input) {
        return Utils.parseInteger(input);
    }

}
