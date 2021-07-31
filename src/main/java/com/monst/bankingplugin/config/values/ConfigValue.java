package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
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

    static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    private final String path;
    private final T defaultConfiguration;
    private T lastSeenValue;

    ConfigValue(String path, T defaultConfiguration) {
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
            setDefault();
            PLUGIN.getLogger().info(String.format("Missing config value \"%s\" was added to the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        } catch (InvalidValueException e) {
            convertAndWriteToFile(e.getReplacement());
            PLUGIN.getLogger().info(String.format("Validated corrupt config value \"%s\" in the config.yml file.", path));
            return lastSeenValue = e.getReplacement();
        } catch (CorruptedValueException e) {
            setDefault();
            PLUGIN.getLogger().info(String.format("Reset corrupt config value \"%s\" to default in the config.yml file.", path));
            return lastSeenValue = defaultConfiguration;
        }
    }

    public final T set(@Nonnull String input) throws ArgumentParseException {
        T newValue = input.isEmpty() ? defaultConfiguration : parse(input);
        beforeSet();
        convertAndWriteToFile(newValue);
        lastSeenValue = newValue;
        afterSet();
        return lastSeenValue;
    }

    void beforeSet() {}
    void afterSet() {}

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

    private void setDefault() {
        convertAndWriteToFile(defaultConfiguration);
    }

    private void convertAndWriteToFile(T t) {
        writeToFile(convertToStorableType(t));
    }

    private void writeToFile(Object o) {
        write(PLUGIN.getConfig(), path, o);
    }

    private T readFromFile() throws MissingValueException, InvalidValueException, CorruptedValueException {
        return read(PLUGIN.getConfig(), path);
    }

}
