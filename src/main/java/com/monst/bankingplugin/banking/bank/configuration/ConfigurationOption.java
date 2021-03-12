package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConfigurationOption<T> {

    T value;
    private boolean customValue;

    ConfigurationOption() {
        this.value = null;
        this.customValue = false;
    }

    ConfigurationOption(T value) {
        this.value = value;
        this.customValue = value != null;
    }

    abstract Config.ConfigPair<T> getConfigPair();

    public T getNullable() {
        return value;
    }

    public T get() {
        if (!customValue)
            return getConfigPair().getDefault();
        return getConfigPair().isOverridable() ? value : getConfigPair().getDefault();
    }

    public String getFormatted() {
        return String.valueOf(get());
    }

    public boolean set(@Nullable String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            customValue = false;
            value = null;
            return true;
        }
        T newValue = parse(input);
        customValue = true;
        value = newValue;
        return getConfigPair().isOverridable();
    }

    abstract T parse(@Nonnull String input) throws ArgumentParseException;

}
