package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Multipliers extends OverridableList<Integer> {

    public Multipliers() {
        super("multipliers", Collections.singletonList(1), FileConfiguration::getIntegerList);
    }

    @Override
    public OverriddenValue<List<Integer>> override(List<Integer> value) {
        return new OverriddenList<Integer>(this, value) {
            @Override
            Stream<Integer> parseToStream(String input) {
                return Arrays.stream(input.replaceAll("\\p{Punct}", " ").split("\\s\\s*"))
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .map(Math::abs);
            }
        };
    }

    @Override
    protected boolean isValid(List<Integer> value) {
        return super.isValid(value) && !value.isEmpty();
    }

}
