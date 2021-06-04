package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.LocalTimeParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends OverridableSet<LocalTime> {

    public InterestPayoutTimes() {
        super("interest-payout-times", Collections.emptySet());
    }

    @Override
    protected void afterNotify() {
        if (PLUGIN.isEnabled())
            PLUGIN.getScheduler().scheduleAll();
    }

    @Override
    public Object convertToSettableType(Set<LocalTime> localTimes) {
        return localTimes.stream().map(LocalTime::toString).collect(Collectors.toList()); // Must convert to string list to set
    }

    @Override
    public Set<LocalTime> readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        Set<LocalTime> times = new LinkedHashSet<>();
        for (String time : config.getStringList(path))
            try {
                times.add(parseSingle(time));
            } catch (LocalTimeParseException ignored) {}
        return times;
    }

    @Override
    public Set<LocalTime> getEmptyCollection() {
        return new LinkedHashSet<>();
    }

    @Override
    public LocalTime parseSingle(String input) throws LocalTimeParseException {
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new LocalTimeParseException(input);
        }
    }

}
