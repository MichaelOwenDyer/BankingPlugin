package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.exceptions.parse.TimeParseException;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.command.CommandSender;

import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends OverridableValue<List<String>, Set<LocalTime>> implements ConfigCollection<LocalTime, Set<LocalTime>> {

    public InterestPayoutTimes(BankingPlugin plugin) {
        super(plugin, "interest-payout-times", Collections.emptySet());
    }

    @Override
    public void afterSet(CommandSender executor) {
        super.afterSet(executor);
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

    public OverriddenValue<Set<LocalTime>> override(Bank bank, Set<LocalTime> value) {
        return new OverriddenValue<Set<LocalTime>>(this, value) {
            @Override
            void afterSet() {
                InterestEventScheduler.scheduleBank(bank);
            }
        };
    }

}
