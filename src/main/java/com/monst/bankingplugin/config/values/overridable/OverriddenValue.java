package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nullable;

public abstract class OverriddenValue<T> {

    protected final OverridableValue<T> attribute;
    protected T value;

    protected OverriddenValue(OverridableValue<T> attribute, T value) {
        this.attribute = attribute;
        if (value == null && Config.stubbornBanks.get())
            value = getConfigAttribute().getDefault();
        this.value = value;
    }

    protected OverridableValue<T> getConfigAttribute() {
        return attribute;
    }

    public T getNullable() {
        return value;
    }

    public T get() {
        return get(false);
    }

    public T get(boolean ignoreConfig) {
        if (value == null)
            return getConfigAttribute().getDefault();
        if (ignoreConfig)
            return value;
        return getConfigAttribute().isOverridable() ? value : getConfigAttribute().getDefault();
    }

    public String getFormatted() {
        return getFormatted(false);
    }

    public String getFormatted(boolean ignoreConfig) {
        return getConfigAttribute().format(get(ignoreConfig));
    }

    public boolean set(@Nullable String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            value = null;
            return true;
        }
        value = getConfigAttribute().parse(input);
        return getConfigAttribute().isOverridable();
    }

}
