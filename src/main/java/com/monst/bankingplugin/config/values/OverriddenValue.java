package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;

public class OverriddenValue<T> {

    private final OverridableValue<?, T> configValue;
    private T customValue;

    OverriddenValue(OverridableValue<?, T> configValue, T customValue) {
        this.configValue = configValue;
        if (customValue == null && Config.stubbornBanks.get())
            customValue = configValue.get();
        this.customValue = customValue;
    }

    public T getCustomValue() {
        return customValue;
    }

    public T get() {
        return get(false);
    }

    private T get(boolean ignoreNonOverridable) {
        if (customValue == null) {
            if (Config.stubbornBanks.get())
                return customValue = configValue.get();
            return configValue.get();
        }
        return (ignoreNonOverridable || configValue.isOverridable()) ? customValue : configValue.get();
    }

    public String getFormatted() {
        return getFormatted(false);
    }

    public String getFormatted(boolean ignoreNonOverridable) {
        return configValue.format(get(ignoreNonOverridable));
    }

    public boolean set(String input) throws ArgumentParseException {
        boolean overrideCompliant;
        if (input == null || input.isEmpty()) {
            customValue = null;
            overrideCompliant = true;
        } else {
            customValue = configValue.parse(input);
            overrideCompliant = configValue.isOverridable();
        }
        afterSet();
        return overrideCompliant;
    }

    void afterSet() {}

}
