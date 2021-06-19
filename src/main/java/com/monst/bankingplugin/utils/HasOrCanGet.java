package com.monst.bankingplugin.utils;

import java.util.function.Supplier;

public class HasOrCanGet<T> implements Supplier<T> {

    private final T t;
    private final Supplier<T> tSupplier;

    public HasOrCanGet(T t) {
        this.t = t;
        this.tSupplier = null;
    }

    public HasOrCanGet(Supplier<T> tSupplier) {
        this.t = null;
        this.tSupplier = tSupplier;
    }

    @Override
    public T get() {
        if (t != null)
            return t;
        if (tSupplier != null)
            return tSupplier.get();
        throw new IllegalStateException("Neither value nor supplier was non-null!");
    }

}
