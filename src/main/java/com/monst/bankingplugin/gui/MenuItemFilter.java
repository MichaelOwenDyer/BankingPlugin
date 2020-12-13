package com.monst.bankingplugin.gui;

import java.util.function.Predicate;

public abstract class MenuItemFilter<T> implements Predicate<T> {

    public abstract String getName();

    public static <T> MenuItemFilter<T> of(String name, Predicate<T> predicate) {
        return new MenuItemFilter<T>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean test(T t) {
                return predicate.test(t);
            }
        };
    }

}
