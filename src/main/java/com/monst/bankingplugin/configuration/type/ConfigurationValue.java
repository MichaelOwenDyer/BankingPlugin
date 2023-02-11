package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.MissingValueException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.configuration.validation.ExceptionBuffer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Supplier;

/**
 * A configuration value stored in the {@code config.yml} file under a certain path.
 * By default, it is assumed that the configuration value is required to be present.
 * If you wish to make a configuration value optional, wrap its type in an {@link Optional}.
 * @param <T> the type of the value
 */
public abstract class ConfigurationValue<T> implements Supplier<T> {

    protected final BankingPlugin plugin;
    private final String path;
    private final T defaultValue;
    private T loadedValue;

    /**
     * Creates a new configuration value of the specified plugin at the specified path in the plugin's {@code config.yml} file.
     * Calling this constructor will immediately load the value from the file, creating it if it doesn't exist.
     * @param plugin the plugin instance
     * @param path the path in the {@code config.yml} file (subsections are demarcated with .)
     * @param defaultValue the default value of this configuration value
     */
    public ConfigurationValue(BankingPlugin plugin, String path, T defaultValue) {
        this.plugin = plugin;
        this.path = path;
        this.defaultValue = validate(defaultValue);
        this.loadedValue = load();
    }

    /**
     * Gets the current value of this configuration value as seen in the {@code config.yml} file.
     * @return the current value
     */
    @Override
    public T get() {
        return loadedValue;
    }

    /**
     * Reloads this configuration value.
     * <p><b>Note:</b></p> it is the responsibility of the developer to call {@link Plugin#reloadConfig()}
     * before using this method, to ensure that the most recent version of the {@code config.yml} file is loaded into memory,
     * as well as {@link Plugin#saveConfig()} afterwards to finally persist any changes into the file.
     * <p>If multiple values are to be reloaded consecutively, it is best practice to call {@link Plugin#reloadConfig() reloadConfig}
     * once at the very beginning, and {@link Plugin#saveConfig() saveConfig} once at the very end.
     */
    @SuppressWarnings("unused")
    public void reload() {
        this.loadedValue = load();
    }

    /**
     * Loads this configuration value from the plugin's current {@link FileConfiguration} using {@link Plugin#getConfig()}.
     * If the value was missing or uninterpretable, the default value will be written to the plugin config and returned.
     * If the value was otherwise imperfectly formed or outside its bounds, a replacement will be written to the plugin config and returned.
     * In any case, by the time this method returns a value, that value will be what is found in the plugin config.
     * @return the loaded value from the config
     */
    private T load() {
        plugin.debug("Loading configuration value " + path);
        try {
            return read(plugin.getConfig(), path);
        } catch (MissingValueException | UnreadableValueException e) {
            plugin.debug("Value of %s was missing or uninterpretable. Writing default value %s", path, format(defaultValue));
            return write(defaultValue);
        } catch (ValueOutOfBoundsException e) {
            plugin.debug("Value of %s was outside its bounds. Writing replacement value %s", path, format(e.getReplacement()));
            return write(e.getReplacement());
        }
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
    @SuppressWarnings("unused")
    public void parseAndSet(String input) throws ArgumentParseException {
        plugin.reloadConfig();
        T newValue = parse(input);
        plugin.debug("Setting configuration value %s to %s, parsed from %s", path, format(newValue), input);
        set(newValue);
        plugin.saveConfig();
    }

    /**
     * Validates and sets this configuration value to a new value.
     * <p><b>Note:</b></p> it is the responsibility of the developer to call {@link Plugin#reloadConfig()}
     * before using this method, to ensure that the most recent version of the {@code config.yml} file is loaded into memory,
     * as well as {@link Plugin#saveConfig()} afterwards to finally persist any changes into the file.
     * <p>If multiple values are to be set consecutively, it is best practice to call {@link Plugin#reloadConfig() reloadConfig}
     * once at the very beginning, and {@link Plugin#saveConfig() saveConfig} once at the very end.
     * @param newValue the new value
     */
    public void set(T newValue) {
        newValue = validate(newValue);
        beforeSet();
        plugin.debug("Setting configuration value %s to %s", path, format(newValue));
        loadedValue = write(newValue);
        afterSet();
    }

    /**
     * Resets this configuration value to the default.
     * <p><b>Note:</b></p> it is the responsibility of the developer to call {@link Plugin#reloadConfig()}
     * before using this method, to ensure that the most recent version of the {@code config.yml} file is loaded into memory,
     * as well as {@link Plugin#saveConfig()} afterwards to finally persist any changes into the file.
     * <p>If multiple values are to be reset consecutively, it is best practice to call {@link Plugin#reloadConfig() reloadConfig}
     * once at the very beginning, and {@link Plugin#saveConfig() saveConfig} once at the very end.
     */
    @SuppressWarnings("unused")
    public void reset() {
        beforeSet();
        plugin.debug("Resetting configuration value %s to default value %s", path, format(defaultValue));
        loadedValue = write(defaultValue);
        afterSet();
    }

    /**
     * Validates the provided value against this configuration value's bounds.
     * If the input adheres to every bound, it is returned with no change.
     * Otherwise, a replacement value is returned.
     * @param value the value to validate
     * @return the input, or a validated replacement
     */
    protected T validate(T value) {
        try {
            plugin.debug("Validating value %s for path %s", format(value), path);
            validate(value, getBounds());
            return value;
        } catch (ValueOutOfBoundsException e) {
            return e.getReplacement();
        }
    }

    /**
     * Writes a value to the plugin {@link FileConfiguration} at the corresponding path.
     * @param t the value to be written
     */
    private T write(T t) {
        plugin.getConfig().set(path, convertToYamlType(t));
        return t;
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
    @SuppressWarnings("unused")
    public boolean isHotSwappable() {
        return true;
    }

    /**
     * Reads and reconstructs the value currently stored in the {@link FileConfiguration} under the given path.
     * @param config the plugin {@link FileConfiguration}, findable with {@link Plugin#getConfig()}
     * @param path the path where the value is located
     * @return the reconstructed value currently stored in the {@code config.yml} file
     * @throws MissingValueException if the value is missing from the file
     * @throws ValueOutOfBoundsException if the value is not within its bounds
     * @throws UnreadableValueException if the value is uninterpretable
     */
    T read(FileConfiguration config, String path)
            throws MissingValueException, ValueOutOfBoundsException, UnreadableValueException {
        Object o = config.get(path, null); // preserve nullability
        if (o == null)
            throw new MissingValueException(); // value is not present in the file
        return new ExceptionBuffer<>(o)
                .convert(this::convert)
                .validate(getBounds())
                .getOrThrow();
    }

    /**
     * Parses a value from a user-entered string.
     * @param input a string representation of the desired object
     * @return the parsed object
     * @throws ArgumentParseException if the input could not be parsed
     */
    protected abstract T parse(String input) throws ArgumentParseException;
    
    /**
     * Takes the {@link Object} that was fetched from the configuration file, and attempts to convert it into the correct type
     * by calling {@link Object#toString() toString} and {@link #parse parsing} it.
     * This method should be overridden in cases where a more appropriate procedure exists.
     * @throws UnreadableValueException if the object is of an unrelated type and could not be converted.
     * @throws ValueOutOfBoundsException if the object was readable but should have been in a different format.
     */
    protected T convert(Object o) throws ValueOutOfBoundsException, UnreadableValueException {
        try {
            return parse(o.toString());
        } catch (ArgumentParseException e) {
            throw new UnreadableValueException();
        }
    }

    /**
     * Throws an {@link ValueOutOfBoundsException} if the provided value does not adhere to one or more of the provided bounds.
     * The exception will contain a replacement value to use instead.
     * @param t the value to validate
     * @throws ValueOutOfBoundsException if the value is outside its bounds
     */
    static <T> void validate(T t, List<Bound<T>> bounds) throws ValueOutOfBoundsException {
        if (!bounds.isEmpty())
            new ExceptionBuffer<>(t).validate(bounds).getOrThrow();
    }

    /**
     * Gets a list of {@link Bound}s that this configuration value is required to conform to.
     * These bounds will be silently enforced at all times. If a value is found to be non-compliant with a bound
     * in the list, the next bound will validate its replacement.
     * By default, this method returns a singleton list of the Bound provided by {@link #getBound()}.
     * Therefore, if the configuration value only requires a single Bound, override that method instead.
     * @return a list of Bounds for this configuration value
     */
    protected List<Bound<T>> getBounds() {
        return Collections.singletonList(getBound());
    }

    /**
     * Gets a single {@link Bound} that this configuration value is required to conform to.
     * If the value is found to be outside this bound, it will be replaced.
     * By default, this method returns a Bound that allows all values to pass through.
     * If more than one bound is needed, override {@link #getBounds()} instead.
     * @return a Bound for this configuration value
     */
    protected Bound<T> getBound() {
        return Bound.alwaysPasses();
    }

    /**
     * Converts the configuration value into a type that can be stored in the {@code config.yml} file.
     * By default, this method returns the value itself.
     * @param t the value
     * @return a file-storable object
     */
    protected Object convertToYamlType(T t) {
        return t;
    }

    /**
     * Gets the path of this configuration value where it can be found in the {@code config.yml} file.
     * @return the path of this configuration value
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the default value of this configuration value as defined in the constructor.
     * @return the default value of this configuration value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets a list of tab-completions to be shown to a player typing in a command.
     * By default, this returns a formatted list of the current value and the default value of this configuration value.
     * This method may be overridden at will; it is not used by the rest of this library.
     * @param player the player typing in the command
     * @param args the arguments the player has typed so far
     * @return a list of tab-completions
     */
    @SuppressWarnings("unused")
    public List<String> getTabCompletions(Player player, String[] args) {
        return Arrays.asList(toString(), format(defaultValue));
    }

    /**
     * Formats this configuration value.
     * @param t the value
     * @return a formatted string representing the state of this configuration value
     */
    protected String format(T t) {
        return String.valueOf(t);
    }

    /**
     * @return the formatted current state of this configuration value
     */
    @Override
    public String toString() {
        return format(get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ConfigurationValue<?> other = (ConfigurationValue<?>) o;
        return Objects.equals(path, other.path) && Objects.equals(defaultValue, other.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, defaultValue);
    }

}
