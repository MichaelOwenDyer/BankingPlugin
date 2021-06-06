package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.LocalTimeParseException;
import com.monst.bankingplugin.utils.InterestEventScheduler;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends OverridableSet<LocalTime> {

    public InterestPayoutTimes() {
        super("interest-payout-times", Collections.emptySet());
    }

    @Override
    protected void afterSet2(Set<LocalTime> newValue) {
        if (PLUGIN.isEnabled())
            InterestEventScheduler.scheduleAll();
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

    @Override
    public OverriddenValue<Set<LocalTime>> override(Set<LocalTime> value) {
        return new OverriddenValue<Set<LocalTime>>(this, value) {
            @Override
            protected void afterSet() {
                InterestPayoutTimes.super.afterSet2(value);
            }
        };
    }

}
