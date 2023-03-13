package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.MissingValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.validation.Bounds;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface Transformer<T> {
    
    T parse(String input) throws ArgumentParseException;
    
    default T convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
        return parse(String.valueOf(object));
    }
    
    default void nullCheck(Object object) throws MissingValueException {
        if (object == null)
            throw new MissingValueException();
    }
    
    default Object toYaml(T value) {
        return value;
    }
    
    default String format(T value) {
        return String.valueOf(value);
    }
    
    default Transformer<T> bounded(Bounds<T> bounds) {
        return new Transformer<T>() {
            @Override
            public T parse(String input) throws ArgumentParseException {
                T value = Transformer.this.parse(input);
                return bounds.replace(value);
            }
            
            @Override
            public T convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
                T value;
                try {
                    value = Transformer.this.convert(object);
                } catch (ValueOutOfBoundsException e) {
                    bounds.validate(e.getReplacement());
                    throw e;
                }
                bounds.validate(value);
                return value;
            }
    
            @Override
            public void nullCheck(Object object) throws MissingValueException {
                Transformer.this.nullCheck(object);
            }
    
            @Override
            public Object toYaml(T value) {
                return Transformer.this.toYaml(value);
            }
            
            @Override
            public String format(T value) {
                return Transformer.this.format(value);
            }
        };
    }
    
    default <C extends Collection<T>> Transformer<C> collect(Supplier<? extends C> collectionFactory) {
        
        return new Transformer<C>() {
            @Override
            public C parse(String input) throws ArgumentParseException {
                C collection = collectionFactory.get();
                for (String s : input.split("\\s*(,|\\s)\\s*"))
                    collection.add(Transformer.this.parse(s));
                return collection;
            }
            
            @Override
            public C convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
                if (!(object instanceof List))
                    throw new UnreadableValueException();
                boolean problemFound = false;
                C collection = collectionFactory.get();
                for (Object element : (List<?>) object) {
                    try {
                        T value = Transformer.this.convert(element);
                        if (collection.add(value))
                            continue;
                    } catch (ValueOutOfBoundsException e) {
                        collection.add(e.getReplacement());
                    } catch (UnreadableValueException ignored) {}
                    problemFound = true;
                }
                if (problemFound)
                    throw new ValueOutOfBoundsException(collection);
                return collection;
            }
    
            @Override
            public Object toYaml(C value) {
                return value.stream().map(Transformer.this::toYaml).collect(Collectors.toList());
            }
    
            @Override
            public String format(C value) {
                return value.stream().map(Transformer.this::format).collect(Collectors.joining(", ")); // do not include brackets
            }
        };
    }
    
    default Transformer<Optional<T>> optional() {
        return new Transformer<Optional<T>>() {
            @Override
            public Optional<T> parse(String input) throws ArgumentParseException {
                if (input.isEmpty())
                    return Optional.empty();
                return Optional.of(Transformer.this.parse(input));
            }
    
            @Override
            public Optional<T> convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
                if (object == null)
                    return Optional.empty();
                return Optional.of(Transformer.this.convert(object));
            }
    
            @Override
            public void nullCheck(Object object) {
                // do nothing
            }
    
            @Override
            public Object toYaml(Optional<T> value) {
                return value.map(Transformer.this::toYaml).orElse(null);
            }
    
            @Override
            public String format(Optional<T> value) {
                return value.map(Transformer.this::format).orElse("");
            }
        };
    }
    
}
