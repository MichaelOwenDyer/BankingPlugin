package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Multipliers extends ListConfigurationOption<Integer> {

    public Multipliers() {
        super();
    }

    public Multipliers(List<Integer> value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<List<Integer>> getConfigPair() {
        return Config.getMultipliers();
    }

    @Override
    Stream<Integer> parseToStream(String input) {
        return Arrays.stream(input.replace(",", " ").split("\\s\\s*"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .map(Math::abs);
    }

}
