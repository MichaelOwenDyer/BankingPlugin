package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import java.util.Collections;
import java.util.List;

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

    public List<String> getTabCompletions() {
        return Collections.singletonList(getFormatted());
    }

    @Override
    public T get() {
        if (lastSeenValue == null)
            PLUGIN.reloadConfig();
            lastSeenValue = readValueFromFile(PLUGIN.getConfig(), path);
        if (lastSeenValue == null) {
            PLUGIN.getConfig().set(path, convertToSettableType(defaultConfiguration));
            PLUGIN.saveConfig();
            lastSeenValue = defaultConfiguration;
        }
        return lastSeenValue;
    }

    public void set(String input) throws ArgumentParseException {
        T newValue = parse(input);
        PLUGIN.reloadConfig();
        PLUGIN.getConfig().set(path, convertToSettableType(newValue));
        PLUGIN.saveConfig();
        clearLastSeen();
        afterSet();
    }

    public void clearLastSeen() {
        lastSeenValue = null;
    }

    protected void afterSet() {}

    @Override
    public boolean isPathMissing() {
        return !PLUGIN.getConfig().contains(path, true);
    }

}