package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

public abstract class ConfigValue<T> implements IConfigValue<T> {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    protected final String path;
    protected final T defaultConfiguration;
    protected T lastSeenValue;

    protected ConfigValue(String path, T defaultConfiguration) {
        this.path = path;
        this.defaultConfiguration = defaultConfiguration;
        this.lastSeenValue = get(); // Initialize value in memory
    }

    public final String getFormatted() {
        return format(get());
    }

    public String format(T t) {
        return String.valueOf(t);
    }

    @Override
    public T get() {
        if (lastSeenValue == null)
            PLUGIN.reloadConfig();
            lastSeenValue = readValueFromFile(PLUGIN.getConfig(), path);
        if (lastSeenValue == null) {
            PLUGIN.getConfig().set(path, defaultConfiguration);
            PLUGIN.saveConfig();
            lastSeenValue = defaultConfiguration;
        }
        return lastSeenValue;
    }

    public void set(String input) throws ArgumentParseException {
        T newValue = parse(input);
        PLUGIN.reloadConfig();
        PLUGIN.getConfig().set(path, newValue);
        PLUGIN.saveConfig();
        clearLastSeen();
        afterSet();
    }

    protected void afterSet() {}

    public void clearLastSeen() {
        lastSeenValue = null;
    }

}
