package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

public abstract class ConfigValue<T> {

    protected static FileConfiguration CONFIG = BankingPlugin.getInstance().getConfig();

    protected T defaultValue;
    protected T lastSeenValue = null;
    BiFunction<FileConfiguration, String, T> valueFinder;

    protected ConfigValue(T defaultValue, BiFunction<FileConfiguration, String, T> valueFinder) {
        this.defaultValue = defaultValue;
        this.valueFinder = valueFinder;
    }

    protected T get() {
        if (lastSeenValue == null)
            lastSeenValue = valueFinder.apply(CONFIG, getPath());
        if (!isValid(lastSeenValue))
            lastSeenValue = defaultValue;
        return lastSeenValue;
    }

    protected boolean isValid(T value) {
        return value != null;
    }

    protected abstract String getPath();

    // public abstract void set(String value) throws ArgumentParseException;

    public abstract void clear();

}
