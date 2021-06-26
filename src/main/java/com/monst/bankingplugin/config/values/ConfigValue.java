package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

import javax.annotation.Nonnull;
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
    public final T get() {
        if (lastSeenValue == null) {
            reload();
            if (isValueMissing()) {
                setDefault();
                save();
            }
            lastSeenValue = readFromFile();
        }
        return lastSeenValue;
    }

    public final void set(@Nonnull String input) throws ArgumentParseException {
        T newValue = input.isEmpty() ? defaultConfiguration : parse(input);
        beforeSet(newValue);
        reload();
        writeToFile(newValue);
        save();
        forgetLastSeen();
        afterSet(newValue);
    }

    public final void forgetLastSeen() {
        lastSeenValue = null;
    }

    protected void beforeSet(T newValue) {}
    protected void afterSet(T newValue) {}

    private void reload() {
        PLUGIN.reloadConfig();
    }

    private void save() {
        PLUGIN.saveConfig();
    }

    private void writeToFile(T t) {
        PLUGIN.getConfig().set(path, convertToSettableType(t));
    }

    private T readFromFile() {
        return readFromFile(PLUGIN.getConfig(), path);
    }

    private void setDefault() {
        writeToFile(defaultConfiguration);
    }

    private boolean isValueMissing() {
        return !PLUGIN.getConfig().isSet(path);
    }

}
