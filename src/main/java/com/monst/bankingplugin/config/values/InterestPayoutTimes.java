package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.exceptions.parse.TimeParseException;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Parser;

import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends OverridableValue<List<String>, Set<LocalTime>> implements ConfigCollection<LocalTime, Set<LocalTime>> {

    public InterestPayoutTimes() {
        super("interest-payout-times", Collections.emptySet());
    }

    @Override
    void afterSet(Set<LocalTime> newValue) {
        super.afterSet(newValue);
        if (PLUGIN.isEnabled())
            InterestEventScheduler.scheduleAllBanks();
    }

    @Override
    public Set<LocalTime> getEmptyCollection() {
        return new LinkedHashSet<>();
    }

    @Override
    public LocalTime parseSingle(String input) throws TimeParseException {
        return Parser.parseLocalTime(input);
    }

    @Override
    public Object convertToStorableType(Set<LocalTime> set) {
        return set.stream().map(LocalTime::toString).collect(Collectors.toList()); // must cast to List<String> in order to set
    }

    @Override
    public OverriddenValue<Set<LocalTime>> override(Bank bank, Set<LocalTime> value) {
        return new OverriddenValue<Set<LocalTime>>(this, value) {
            @Override
            void afterSet() {
                InterestEventScheduler.scheduleBank(bank);
            }
        };
    }

}
