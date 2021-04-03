package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.MemorySection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("multipliers", Collections.singletonList(1), Multipliers::getMultiplierList);
    }

    private static List<Integer> getMultiplierList(MemorySection config, String path) {
        List<Integer> multipliers = config.getIntegerList(path);
        return multipliers.isEmpty() ? Collections.singletonList(1) : multipliers;
    }

    @Override
    Stream<Integer> parseToStream(String input) {
        return Arrays.stream(input.replaceAll("\\p{Punct}", " ").split("\\s\\s*"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .map(Math::abs);
    }

    @Override
    protected boolean isValid(List<Integer> value) {
        return value != null && !value.isEmpty();
    }

}
