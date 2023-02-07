package com.monst.bankingplugin.configuration.validation;

import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link Bound} is a {@link Predicate} that checks whether a value is within a certain range.
 * If it is not, it replaces the value with a different one by throwing a {@link ValueOutOfBoundsException}.
 * @param <T> the type of the value to check
 */
@FunctionalInterface
public interface Bound<T> {

    /**
     * Check if a value falls within the {@link Bound}, throwing a {@link ValueOutOfBoundsException} if not.
     * @param t the value to check
     * @throws ValueOutOfBoundsException if the provided value does not comply with this {@link Bound}
     */
    void check(T t) throws ValueOutOfBoundsException;

    /**
     * @return a {@link Bound} that always passes
     */
    static <T> Bound<T> alwaysPasses() {
        return t -> {};
    }

    /**
     * Returns a bound which requires the {@link Comparable} value to be at least {@code min}.
     * If not, the value is replaced with {@code min}.
     * @param min the lower bound
     * @param <T> the type of value
     * @return an inclusive lower bound of {@code min}
     */
    static <T extends Comparable<T>> Bound<T> atLeast(T min) {
        return requiring(t -> t.compareTo(min) >= 0, t -> min);
    }

    /**
     * Returns a bound which requires the {@link Comparable} value to be at most {@code max}.
     * If not, the value is replaced with {@code max}.
     * @param max the lower bound
     * @param <T> the type of value
     * @return an inclusive upper bound of {@code max}
     */
    static <T extends Comparable<T>> Bound<T> atMost(T max) {
        return requiring(t -> t.compareTo(max) <= 0, t -> max);
    }
    
    /**
     * Returns a bound which requires the value to fulfill the provided predicate,
     * mapping it to a replacement value if it does not.
     * In a typical use case, the mapper produces values which consistently fulfill the predicate.
     * @param shouldBe the predicate which values are required to pass
     * @param replacementMapper the function which maps an out-of-bounds value to a replacement
     * @param <T> the type of bound
     * @return a bound requiring the specified predicate and mapping noncompliant values using the specified function
     */
    static <T> Bound<T> requiring(Predicate<? super T> shouldBe, Function<T, T> replacementMapper) {
        return t -> {
            if (!shouldBe.test(t))
                throw new ValueOutOfBoundsException(replacementMapper.apply(t));
        };
    }

    /**
     * Returns a bound which forbids the value from fulfilling the provided predicate,
     * mapping it to a replacement value if it does.
     * In a typical use case, the mapper produces values which consistently do not fulfill the predicate.
     * @param shouldNotBe the predicate which values are forbidden from passing
     * @param replacementMapper the function which maps an out-of-bounds value to a replacement
     * @param <T> the type of bound
     * @return a bound forbidding the specified predicate and mapping noncompliant values using the specified function
     */
    static <T> Bound<T> disallowing(Predicate<? super T> shouldNotBe, Function<T, T> replacementMapper) {
        return t -> {
            if (shouldNotBe.test(t))
                throw new ValueOutOfBoundsException(replacementMapper.apply(t));
        };
    }

}
