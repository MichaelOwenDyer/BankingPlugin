package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class ConfigurationOption<T> implements Supplier<T> {

    T value;

    ConfigurationOption() {
        this.value = Config.stubbornBanks ? getConfigPair().getDefault() : null;
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
        return getConfigPair().isOverridable();
    }

    abstract T parse(@Nonnull String input) throws ArgumentParseException;

}
