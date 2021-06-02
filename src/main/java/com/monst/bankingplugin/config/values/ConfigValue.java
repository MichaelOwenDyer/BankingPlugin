package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class ConfigValue<T> implements IConfigValue<T> {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();
    protected static final FileConfiguration CONFIG = PLUGIN.getConfig();

    protected final String path;
    protected final T defaultConfiguration;
    protected T lastSeenValue = null;

    protected ConfigValue(String path, T defaultConfiguration) {
        this.path = path;
        this.defaultConfiguration = defaultConfiguration;
    }

    public final String getFormatted() {
        return format(get());
    }

    public String format(T t) {
        return String.valueOf(t);
    }

    @Override
    public T get() {
        // if (lastSeenValue == null)
            lastSeenValue = readValueFromFile(CONFIG, path);
        if (lastSeenValue == null)
            lastSeenValue = defaultConfiguration;
        return lastSeenValue;
    }

    public void parseAndSet(String input) throws ArgumentParseException {
        set(parse(input));
    }

    public void set(T input) {
        CONFIG.set(path, input);
        lastSeenValue = null;
    }

}
