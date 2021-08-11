package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A value stored in the config.yml file under a certain path.
 * @param <V> the type the value is stored as in the file
 * @param <T> the real type of the config value
 */
public abstract class ConfigValue<V, T> implements ConfigurationValue<V, T> {

    final BankingPlugin plugin;
    private final String path;
    private final T defaultConfiguration;
    private T lastSeenValue;

    ConfigValue(BankingPlugin plugin, String path, T defaultConfiguration) {
        this.plugin = plugin;
        this.path = path;
        this.defaultConfiguration = defaultConfiguration;
        get(); // Initialize value in memory
    }

    @Override
    public final T get() {
        if (lastSeenValue != null)
            return lastSeenValue;
        try {
            return lastSeenValue = readFromFile();
        } catch (MissingValueException e) {
            writeToFile(defaultConfiguration);
            plugin.getLogger().info(String.format("Missing config value \"%s\" was added to the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        } catch (InvalidValueException e) {
            writeToFile(e.getValidatedValue());
            plugin.getLogger().info(String.format("Validated corrupt config value \"%s\" in the config.yml file.", path));
            return lastSeenValue = e.getValidatedValue();
        } catch (CorruptedValueException e) {
            writeToFile(defaultConfiguration);
            plugin.getLogger().info(String.format("Reset corrupt config value \"%s\" to default in the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        }
    }

    public final void set(String input) throws ArgumentParseException {
        plugin.reloadConfig();
        T newValue = input.isEmpty() && nonOptional() ? defaultConfiguration : parse(input);
        beforeSet();
        writeToFile(newValue);
        lastSeenValue = newValue;
        new PluginConfigureEvent(this, newValue).fire();
        plugin.saveConfig();
    }

    boolean nonOptional() {
        return true;
    }

    void beforeSet() {}
    public void afterSet(CommandSender executor) {}

    public final void reload() {
        lastSeenValue = null;
        get();
    }

    public boolean isHotSwappable() {
        return true;
    }

    public final String getFormatted() {
        return format(get());
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Stream.of(getFormatted(), format(defaultConfiguration)).distinct().collect(Collectors.toList());
        return Collections.emptyList();
    }

    public String getPath() {
        return path;
    }

    private void writeToFile(T t) {
        write(plugin.getConfig(), path, convertToStorableType(t));
    }

    private T readFromFile() throws MissingValueException, InvalidValueException, CorruptedValueException {
        return read(plugin.getConfig(), path);
    }

}
