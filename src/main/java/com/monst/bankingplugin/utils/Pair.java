package com.monst.bankingplugin.utils;

import java.util.Objects;

public abstract class Pair<T, K> {

    private T first;
    private K second;

    public Pair(T t, K k) {
        this.first = t;
        this.second = k;
    }

    protected T getFirst() {
        return first;
    }

    protected void setFirst(T first) {
        this.first = first;
    }

    protected K getSecond() {
        return second;
    }

    protected void setSecond(K second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
