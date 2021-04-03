package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ConfigValue<T> implements Supplier<T> {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();
    protected static final FileConfiguration CONFIG = PLUGIN.getConfig();

    protected final T defaultValue;
    protected T lastSeenValue = null;
    private final BiFunction<FileConfiguration, String, T> valueFinder;

    protected ConfigValue(T defaultValue, BiFunction<FileConfiguration, String, T> valueFinder) {
        this.defaultValue = defaultValue;
        this.valueFinder = valueFinder;
    }

    public final String getFormatted() {
        return format(get());
    }

    public String format(T t) {
        return String.valueOf(t);
    }

    public T get() {
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

    public abstract List<String> getPaths();

    public void set(String path, String input) throws ArgumentParseException {
        CONFIG.set(path, parse(input));
        clear();
    }

    public abstract T parse(String input) throws ArgumentParseException;

    protected abstract void clear();

}
