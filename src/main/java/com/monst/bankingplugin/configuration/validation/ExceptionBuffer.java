package com.monst.bankingplugin.configuration.validation;

import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;

import java.util.List;

public class ExceptionBuffer<T> {

    @FunctionalInterface
    public interface Converter<T> {
        T convert(Object o) throws ValueOutOfBoundsException, UnreadableValueException;
    }

    private T latestValue;
    private ValueOutOfBoundsException exception;

    public ExceptionBuffer(T startValue) {
        this.latestValue = startValue;
        this.exception = null;
    }

    private ExceptionBuffer(T value, ValueOutOfBoundsException exception) {
        this.latestValue = value;
        this.exception = exception;
    }

    public <R> ExceptionBuffer<R> convert(Converter<R> converter) throws UnreadableValueException {
        try {
            return new ExceptionBuffer<>(converter.convert(latestValue), null);
        } catch (ValueOutOfBoundsException e) {
            return new ExceptionBuffer<>(e.getReplacement(), e);
        }
    }

    public ExceptionBuffer<T> validate(List<Bound<T>> bounds) {
        for (Bound<T> bound : bounds)
            try {
                bound.check(latestValue);
            } catch (ValueOutOfBoundsException e) {
                latestValue = e.getReplacement();
                if (exception != null)
                    e.addSuppressed(exception);
                exception = e;
            }
        return this;
    }

    public T getOrThrow() throws ValueOutOfBoundsException {
        if (exception != null)
            throw exception;
        return latestValue;
    }

}
