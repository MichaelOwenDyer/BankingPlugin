package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.configuration.validation.ExceptionBuffer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A configuration value with multiple elements, always stored as a list in the config.yml.
 * @param <E> the type of each element
 * @param <T> the type of the container collection
 */
public abstract class ConfigurationCollection<E, T extends Collection<E>> extends ConfigurationValue<T> {

    public ConfigurationCollection(BankingPlugin plugin, String path, T defaultValue) {
        super(plugin, path, defaultValue);
    }

    /**
     * Parses a collection of values from a user-entered string.
     * The individual elements may be separated by spaces, commas, or both.
     * @param input a string representation of the desired object
     * @return the parsed elements in a new collection
     * @throws ArgumentParseException if an element could not be parsed
     */
    @Override
    protected T parse(String input) throws ArgumentParseException {
        T collection = createCollection();
        for (String s : input.split(getInputSplitRegex()))
            collection.add(parseElement(s));
        return collection;
    }

    /**
     * Returns a regular expression String which is used to split the user input into individual elements.
     * By default, this method returns {@code "\\s*,?\\s*"}, which splits by commas, or spaces, or both.
     * @return a regular expression to split user input for parsing
     */
    protected String getInputSplitRegex() {
        return "\\s*(,|\\s)\\s*"; // the user input may be separated by commas or spaces or both
    }

    /**
     * Parses a collection of objects from a list of objects in the configuration file.
     * Each stored object is parsed into an object, validated, and added to a collection.
     * In contrast to {@link #parse(String)}, {@link ArgumentParseException}s will not interrupt the process.
     * If any problems are found while parsing, validating, and adding the objects to the collection, this will be
     * recorded in {@code problemFound} and dealt with later.
     * If a problem was found, an exception will be thrown.
     * If anything of use was reconstructed, this exception will be an {@link ValueOutOfBoundsException} with the
     * collection as a replacement, and the configuration file will be updated to match this collection.
     * If nothing of use could be reconstructed, a {@link UnreadableValueException} will be thrown and the value
     * in the configuration file will be reset to default.
     * @param o a list of the stored type from the configuration file
     * @return a collection of parsed and validated values
     * @throws ValueOutOfBoundsException if there was a problem with at least one value in the configuration file.
     */
    protected T convert(Object o) throws ValueOutOfBoundsException {
        boolean problemFound = false;
        List<?> list = (o instanceof List) ? (List<?>) o : Collections.singletonList(o);
        T collection = createCollection();
        for (Object element : list) {
            try {
                E e = new ExceptionBuffer<>(element)
                        .convert(this::convertElement)
                        .validate(getElementBounds())
                        .getOrThrow();
                if (!collection.add(e))
                    problemFound = true;
            } catch (ValueOutOfBoundsException e) {
                collection.add(e.getReplacement());
                problemFound = true;
            } catch (UnreadableValueException e) {
                problemFound = true;
            }
        }
        if (problemFound)
            throw new ValueOutOfBoundsException(collection);
        return collection;
    }

    /**
     * Validates every element of the provided collection against this configuration value's element bounds,
     * as well as the collection itself against this configuration value's bounds.
     * If the input adheres to every bound, it is returned with no change.
     * Otherwise, a replacement value is returned.
     * @param collection the value to validate
     * @return the input, or a validated replacement
     */
    @Override
    protected T validate(T collection) {
        T validated = createCollection();
        for (E element : collection) {
            try {
                validate(element, getElementBounds());
                validated.add(element);
            } catch (ValueOutOfBoundsException e) {
                validated.add(e.getReplacement());
            }
        }
        try {
            validate(validated, getBounds());
            return validated;
        } catch (ValueOutOfBoundsException e) {
            return e.getReplacement();
        }
    }

    /**
     * Creates a new instance of the specific collection implementation to wrap the elements in.
     * The only method this collection must implement is {@link Collection#add(Object) add}.
     * The return values of this collection's {@link Collection#add(Object) add} method will be observed
     * when adding elements; that is, if not all elements could be added, then the config.yml will be updated
     * with only those elements that were successfully added.
     * @return a new instance of the desired collection
     */
    protected abstract T createCollection();

    /**
     * Parses an element from a user-entered string.
     * @param input a string representation of the desired object
     * @return the parsed object
     * @throws ArgumentParseException if the input could not be parsed
     */
    protected abstract E parseElement(String input) throws ArgumentParseException;

    /**
     * Takes an {@link Object} element from the list that was fetched from the configuration file, and attempts to
     * convert it into the correct type.
     * This method should be overridden in cases where a more appropriate procedure exists.
     * @throws UnreadableValueException if the object is of an unrelated type and could not be converted.
     * @throws ValueOutOfBoundsException if the object was readable but should have been in a different format.
     */
    protected E convertElement(Object o)
            throws ValueOutOfBoundsException, UnreadableValueException {
        try {
            return parseElement(o.toString());
        } catch (ArgumentParseException e) {
            throw new UnreadableValueException();
        }
    }

    /**
     * Gets a list of {@link Bound}s that every element of this configuration collection is required to conform to.
     * If an element is found to be outside one or more of these bounds, it will be replaced.
     * By default, this method returns a singleton list of the Bound provided by {@link #getElementBound()}.
     * Therefore, if the collection elements only require a single Bound, override that method instead.
     * @return a list of Bounds for each collection element
     */
    protected List<Bound<E>> getElementBounds() {
        return Collections.singletonList(getElementBound());
    }

    /**
     * Gets a single {@link Bound} that every element of this configuration collection is required to conform to.
     * If an element is found to be outside this bound, it will be replaced.
     * By default, this method returns a Bound that allows all values to pass through.
     * If more than one bound is needed for the collection elements, override {@link #getElementBounds()} instead.
     * @return a Bound for all collection elements
     */
    protected Bound<E> getElementBound() {
        return Bound.alwaysPasses();
    }

    @Override
    protected String format(T collection) {
        return collection.stream().map(this::formatElement).collect(Collectors.joining(", ")); // do not include brackets
    }

    protected String formatElement(E e) {
        return String.valueOf(e);
    }

}
