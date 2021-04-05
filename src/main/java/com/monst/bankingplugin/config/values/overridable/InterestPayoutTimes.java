package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.time.LocalTime;
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
    public List<LocalTime> parse(String input) throws ArgumentParseException {
        return map(Arrays.stream(input.replaceAll("\\p{Punct}", " ").split("\\s\\s*")));
    }

    private static List<LocalTime> getLocalTimes(MemoryConfiguration config, String path) {
        return map(config.getStringList(path).stream());
    }

    private static List<LocalTime> map(Stream<String> stream) {
        return stream
                .filter(Objects::nonNull)
                .map(Utils::parseLocalTime)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}
