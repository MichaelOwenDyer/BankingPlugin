package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class InterestPayoutTimes extends ListConfigurationOption<LocalTime> {

    public InterestPayoutTimes() {
        super();
    }

    public InterestPayoutTimes(List<LocalTime> value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<List<LocalTime>> getConfigPair() {
        return Config.getInterestPayoutTimes();
    }

    @Override
    Stream<LocalTime> parseToStream(String input) {
        return Arrays.stream(input.replace(",", " ").split("\\s\\s*"))
                .filter(s -> !s.isEmpty())
                .map(LocalTime::parse)
                .distinct()
                .sorted();
    }

}
