package com.monst.bankingplugin.configuration;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.MissingValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.transform.Transformer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A configuration value stored in the {@code config.yml} file under a certain path.
 * By default, it is assumed that the configuration value is required to be present.
 * If you wish to make a configuration value optional, wrap its type in an {@link Optional}.
 * @param <T> the type of the value
 */
public class ConfigurationValue<T> extends ConfigurationNode {

    protected final BankingPlugin plugin;
    private final T defaultValue;
    private T value;
    private final Transformer<T> transformer;

    /**
     * Creates a new configuration value at the specified path in the plugin's configuration file. Calling this
     * constructor will immediately load the value from the file, creating it if it doesn't exist.
     *
     * @param plugin       the plugin instance
     * @param key          the relative path of this value
     * @param defaultValue the default value of this configuration value
     * @param transformer  the transformer used for converting the data to and from the desired type
     */
    public ConfigurationValue(BankingPlugin plugin, String key, T defaultValue, Transformer<T> transformer) {
        super(key);
        this.plugin = plugin;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.transformer = transformer;
    }
    
    /**
     * Gets the current value of this configuration value.
     * @return the current value
     */
    public final T get() {
        return value;
    }
    
    /**
     * Gets the default value of this configuration value as defined in the constructor.
     * @return the default value of this configuration value
     */
    public final T getDefaultValue() {
        return defaultValue;
    }
    
    public final Transformer<T> getTransformer() {
        return transformer;
    }
    
    @Override
    protected final void populate(Object object) {
        try {
            transformer.nullCheck(object);
            this.value = transformer.convert(object);
            plugin.debug("Loading configuration value " + key + ": " + transformer.toYaml(value));
        } catch (ValueOutOfBoundsException e) {
            plugin.debug("Value of %s was outside its bounds. Using replacement value %s", key, transformer.toYaml(e.getReplacement()));
            this.value = e.getReplacement();
        } catch (MissingValueException | UnreadableValueException e) {
            plugin.debug("Value of %s was missing or uninterpretable. Using default value %s", key, transformer.toYaml(defaultValue));
            this.value = defaultValue;
        }
    }
    
    @Override
    protected final Object getAsYaml() {
        return transformer.toYaml(value);
    }
    
    /**
     * Parses a user-entered string to a new value, and sets this configuration value.
     * <p><b>Note:</b></p> unlike other methods, this method automatically calls {@link Plugin#reloadConfig()} before and
     * {@link Plugin#saveConfig()} after performing the set operation, under the assumption that
     * parsing user input will not happen inside a loop.
     * Changes will be reflected in the {@code config.yml} file immediately.
     * @param input user input to be parsed, null if the value should be reset
     * @throws ArgumentParseException if the input could not be parsed
     */
    public final void feed(String input) throws ArgumentParseException {
        T value = transformer.parse(input);
        plugin.debug("Setting configuration value %s to %s", key, transformer.toYaml(value));
        beforeSet();
        this.value = value;
        afterSet();
    }

    /**
     * An action to be taken before every time this configuration value is set to a new value.
     */
    protected void beforeSet() {

    }

    /**
     * An action to be taken after every time this configuration value is set to a new value.
     */
    protected void afterSet() {

    }

    /**
     * Returns true if this configuration value can be changed and take effect at runtime, without a server restart.
     * This method may be overridden at will; it is not used by the rest of this library.
     * @return true if this value can be hot-swapped at runtime, false if it requires a restart.
     */
    public boolean isHotSwappable() {
        return true;
    }

    /**
     * Gets a list of tab-completions to be shown to a player typing in a command.
     * By default, this returns a formatted list of the current value and the default value of this configuration value.
     * @param player the player typing in the command
     * @param args the arguments the player has typed so far
     * @return a list of tab-completions
     */
    public List<String> getTabCompletions(Player player, String[] args) {
        return Arrays.asList(transformer.format(get()), transformer.format(defaultValue));
    }

    /**
     * @return the formatted current state of this configuration value
     */
    @Override
    public final String toString() {
        return transformer.format(get());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationValue<?> that = (ConfigurationValue<?>) o;
        return this.key.equals(that.key) && Objects.equals(this.defaultValue, that.defaultValue);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(key, defaultValue);
    }

}
