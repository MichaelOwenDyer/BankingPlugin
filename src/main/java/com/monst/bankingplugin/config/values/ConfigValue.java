package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ConfigValue<T> implements IConfigValue<T> {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    protected final String path;
    protected final T defaultConfiguration;
    protected T lastSeenValue;

    protected ConfigValue(String path, T defaultConfiguration) {
        this.path = path;
        this.defaultConfiguration = defaultConfiguration;
        get(); // Initialize value in memory
    }

    public final String getFormatted() {
        return format(get());
    }

    public List<String> getTabCompletions() {
        return Collections.singletonList(getFormatted());
    }

    @Override
    public final T get() {
        if (lastSeenValue != null)
            return lastSeenValue;
        reload();
        if (isValueMissing()) {
            setDefault();
            PLUGIN.getLogger().info(String.format("Missing config value \"%s\" was added to the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        }
        try {
            return lastSeenValue = Objects.requireNonNull(readFromFile());
        } catch (CorruptedValueException e) {
            if (e.hasReplacement())
                setT(e.getReplacement());
            else
                setDefault();
            PLUGIN.getLogger().info(String.format("Revalidated corrupt config value \"%s\" in the config.yml file.", path));
            return lastSeenValue = e.getReplacement();
        }
    }

    public final boolean set(@Nonnull String input) throws ArgumentParseException {
        T newValue = input.isEmpty() ? defaultConfiguration : parse(input);
        beforeSet(newValue);
        reload();
        setT(newValue);
        forgetLastSeen();
        afterSet(newValue);
        return isHotSwappable();
    }

    public final void forgetLastSeen() {
        lastSeenValue = null;
    }

    protected void beforeSet(T newValue) {}
    protected void afterSet(T newValue) {}
    protected boolean isHotSwappable() {
        return true;
    }

    private void setDefault() {
        setT(defaultConfiguration);
    }

    private void setT(T t) {
        setObject(convertToSettableType(t));
    }

    private void setObject(Object o) {
        PLUGIN.getConfig().set(path, o);
        save();
    }

    private void save() {
        PLUGIN.saveConfig();
    }

    private void reload() {
        PLUGIN.reloadConfig();
    }

    private T readFromFile() throws CorruptedValueException {
        return readFromFile(PLUGIN.getConfig(), path);
    }

    private boolean isValueMissing() {
        return !PLUGIN.getConfig().isSet(path);
    }

}
