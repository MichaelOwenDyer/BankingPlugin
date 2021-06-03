package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InterestPayoutTimes extends OverridableList<LocalTime> {

    public InterestPayoutTimes() {
        super("interest-payout-times", Collections.emptyList());
    }

    @Override
    protected void afterNotify() {
        if (PLUGIN.isEnabled())
            PLUGIN.getScheduler().scheduleAll();
    }

    @Override
    public List<LocalTime> readValueFromFile(MemoryConfiguration config, String path) {
        return config.getStringList(path).stream()
                .map(this::parseSingle)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public LocalTime parseSingle(String input) {
        return Utils.parseLocalTime(input);
    }

}
