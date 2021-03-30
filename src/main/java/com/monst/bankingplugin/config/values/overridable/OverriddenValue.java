package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class OverriddenValue<T> {

    protected OverridableValue<T> attribute;
    protected T value;

    protected OverriddenValue(OverridableValue<T> attribute, T value) {
        this.attribute = attribute;
        if (value == null && Config.stubbornBanks.get())
            value = getSuper().getDefault();
        this.value = value;
    }

    protected OverridableValue<T> getSuper() {
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
            return getSuper().getDefault();
        if (ignoreConfig)
            return value;
        return getSuper().isOverridable() ? value : getSuper().getDefault();
    }

    public String getFormatted() {
        return getFormatted(false);
    }

    public String getFormatted(boolean ignoreConfig) {
        return format(get(ignoreConfig));
    }

    String format(T value) {
        return String.valueOf(value);
    }

    public boolean set(@Nullable String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            value = null;
            return true;
        }
        value = parse(input);
        return getSuper().isOverridable();
    }

    abstract T parse(@Nonnull String input) throws ArgumentParseException;

}
