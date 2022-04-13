package com.monst.bankingplugin.util;

import com.monst.bankingplugin.gui.GUI;

import java.util.Set;

public interface Observable {

    Set<GUI<?>> getObservers();

    default void addObserver(GUI<?> observer) {
        getObservers().add(observer);
    }

    default void removeObserver(GUI<?> observer) {
        getObservers().remove(observer);
    }

    default void notifyObservers() {
        getObservers().forEach(GUI::update);
    }

}
