package com.monst.bankingplugin.utils;

public abstract class Pair<T,K> {

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
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public int hashCode() {
        return ((first.hashCode() * 31) + second.hashCode()) * 31;
    }
}
