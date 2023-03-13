package com.monst.bankingplugin.configuration.validation;

import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;

@FunctionalInterface
public interface Bounds<T> {
    
    /**
     * Check if a value falls within the {@link Bound}, throwing a {@link ValueOutOfBoundsException} if not.
     * @param t the value to check
     * @throws ValueOutOfBoundsException if the provided value does not comply with this {@link Bound}
     */
    void validate(T t) throws ValueOutOfBoundsException;
    
    default T replace(T t) {
        try {
            validate(t);
            return t;
        } catch (ValueOutOfBoundsException e) {
            return e.getReplacement();
        }
    }
    
    default Bounds<T> and(Bound<T> next) {
        return t -> {
            ValueOutOfBoundsException exception = null;
            try {
                this.validate(t);
            } catch (ValueOutOfBoundsException e) {
                t = e.getReplacement();
                exception = e;
            }
            try {
                next.validate(t);
            } catch (ValueOutOfBoundsException e) {
                if (exception != null)
                    e.addSuppressed(exception);
                exception = e;
            }
            if (exception != null)
                throw exception;
        };
    }

}
