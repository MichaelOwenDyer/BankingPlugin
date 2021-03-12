package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConfigurationOption<T> {

    T value;

    ConfigurationOption() {
        this.value = null;
    }

    ConfigurationOption(T value) {
        this.value = value;
    }

    abstract Config.ConfigPair<T> getConfigPair();

    public T getNullable() {
        return value;
    }

    public T get() {
        return get(false);
    }

    public T get(boolean ignoreConfig) {
        if (value == null)
            return getConfigPair().getDefault();
        if (ignoreConfig)
            return value;
        return getConfigPair().isOverridable() ? value : getConfigPair().getDefault();
    }

    public String getFormatted() {
        return getFormatted(false);
    }

    public String getFormatted(boolean ignoreConfig) {
        return String.valueOf(get(ignoreConfig));
    }

    public boolean set(@Nullable String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            value = null;
            return true;
        }
        value = parse(input);
        return getConfigPair().isOverridable();
    }

    abstract T parse(@Nonnull String input) throws ArgumentParseException;

}
