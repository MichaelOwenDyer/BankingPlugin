package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * A value stored in the config.yml file under a certain path.
 * @param <V> the type the value is stored as in the file
 * @param <T> the real type of the config value
 */
public abstract class ConfigValue<V, T> implements IConfigValue<V, T> {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    protected final String path;
    protected final T defaultConfiguration;
    protected T lastSeenValue;

    protected ConfigValue(String path, T defaultConfiguration) {
        this.path = path;
        this.defaultConfiguration = defaultConfiguration;
        get(); // Initialize value in memory
    }

    public String getPath() {
        return path;
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
        try {
            return lastSeenValue = readFromFile();
        } catch (MissingValueException e) {
            setDefault();
            PLUGIN.getLogger().info(String.format("Missing config value \"%s\" was added to the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        } catch (CorruptedValueException e) {
            if (e.hasReplacement()) {
                setT(e.getReplacement());
                PLUGIN.getLogger().info(String.format("Validated corrupt config value \"%s\" in the config.yml file.", path));
                return lastSeenValue = e.getReplacement();
            } else {
                setDefault();
                PLUGIN.getLogger().info(String.format("Reset corrupt config value \"%s\" to default in the config.yml file.", path));
                return lastSeenValue = defaultConfiguration;
            }
        }
    }

    public final void set(@Nonnull String input) throws ArgumentParseException {
        T newValue = input.isEmpty() ? defaultConfiguration : parse(input);
        beforeSet(newValue);
        reload();
        setT(newValue);
        forgetLastSeen();
        afterSet(newValue);
        new PluginConfigureEvent(this, newValue).fire();
    }

    public final void forgetLastSeen() {
        lastSeenValue = null;
    }

    protected void beforeSet(T newValue) {}
    protected void afterSet(T newValue) {}

    public boolean isHotSwappable() {
        return true;
    }

    private void setDefault() {
        setT(defaultConfiguration);
    }

    private void setT(T t) {
        setObject(convertToConfigType(t));
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

    private T readFromFile() throws MissingValueException, CorruptedValueException {
        return read(PLUGIN.getConfig(), path);
    }

}
