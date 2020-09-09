package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.gui.Gui;

import java.util.HashSet;
import java.util.Set;

public interface Observable {

    Set<Gui<?>> observers = new HashSet<>();

    default void addObserver(Gui<?> observer) {
        observers.add(observer);
    }

    default void removeObserver(Gui<?> observer) {
        observers.remove(observer);
    }

    default void notifyObservers() {
        observers.forEach(Gui::update);
    }

}
