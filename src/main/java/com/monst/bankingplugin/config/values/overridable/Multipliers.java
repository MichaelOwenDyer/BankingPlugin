package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemorySection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("multipliers", Collections.singletonList(1), Multipliers::getMultipliers);
    }

    private static List<Integer> getMultipliers(MemorySection config, String path) {
        List<Integer> multipliers = config.getIntegerList(path);
        return multipliers.isEmpty() ? Collections.singletonList(1) : multipliers;
    }

    @Override
    public List<Integer> parse(String input) {
        return Arrays.stream(input.replaceAll("\\p{Punct}", " ").split("\\s\\s*"))
                .filter(s -> !s.isEmpty())
                .map(Utils::parseInteger)
                .filter(Objects::nonNull)
                .map(Math::abs)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean isValid(List<Integer> value) {
        return value != null && !value.isEmpty();
    }

}
