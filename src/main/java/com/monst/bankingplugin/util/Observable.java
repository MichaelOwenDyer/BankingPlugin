package com.monst.bankingplugin.util;

import java.util.Set;

public interface Observable {

    Set<Observer> getObservers();

    default void subscribe(Observer observer) {
        getObservers().add(observer);
    }

    default void unsubscribe(Observer observer) {
        getObservers().remove(observer);
    }

    default void notifyObservers() {
        getObservers().forEach(Observer::update);
    }

}
