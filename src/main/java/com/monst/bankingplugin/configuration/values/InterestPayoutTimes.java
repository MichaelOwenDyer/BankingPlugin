package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.ConfigurationCollection;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends ConfigurationCollection<LocalTime, Set<LocalTime>> implements BankPolicy<Set<LocalTime>> {

    private final BankingPlugin plugin;
    public final AllowOverride allowOverride;

    public InterestPayoutTimes(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("interest-payout-times"), Collections.emptySet());
        this.plugin = plugin;
        this.allowOverride = new AllowOverride(plugin, "interest-payout-times") {
            @Override
            protected void afterSet() {
                InterestPayoutTimes.this.afterSet();
            }
        };
    }

    @Override
    public Set<LocalTime> createCollection() {
        return new LinkedHashSet<>();
    }

    @Override
    public LocalTime parseElement(String input) throws ArgumentParseException {
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new ArgumentParseException(Message.NOT_A_TIME.with(Placeholder.INPUT).as(input).translate(plugin));
        }
    }

    @Override
    public void afterSet() {
        plugin.getSchedulerService().scheduleAll();
    }

    @Override
    protected Object convertToFileData(Set<LocalTime> localTimes) {
        return localTimes.stream().map(LocalTime::toString).collect(Collectors.toList());
    }

    @Override
    public Set<LocalTime> at(Bank bank) {
        if (bank.getInterestPayoutTimes() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setInterestPayoutTimes(get());
            return get();
        }
        return allowOverride.get() ? bank.getInterestPayoutTimes() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setInterestPayoutTimes(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setInterestPayoutTimes(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getInterestPayoutTimes()).orElseGet(this));
    }

}
