package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterestPayoutTimes extends OverridableList<LocalTime> {

    public InterestPayoutTimes() {
        super("interest-payout-times", Collections.emptyList(), InterestPayoutTimes::getLocalTimes);
    }

    @Override
    public void set(String path, String input) throws ArgumentParseException {
        super.set(path, input);
        if (BankingPlugin.getInstance().isEnabled())
            BankingPlugin.getInstance().getScheduler().scheduleAll();
    }

    @Override
    Stream<LocalTime> parseToStream(String input) {
        return Arrays.stream(input.replaceAll("\\p{Punct}", " ").split("\\s\\s*"))
                .filter(s -> !s.isEmpty())
                .map(InterestPayoutTimes::parseLocalTime)
                .filter(Objects::nonNull)
                .distinct()
                .sorted();
    }

    private static List<LocalTime> getLocalTimes(MemoryConfiguration config, String path) {
        return config.getStringList(path).stream()
                .map(InterestPayoutTimes::parseLocalTime)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static LocalTime parseLocalTime(String string) {
        try {
            return LocalTime.parse(string);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
