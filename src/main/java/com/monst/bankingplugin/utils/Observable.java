package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    private final Set<GUI<?>> observers = new HashSet<>();
    private int queuedUpdates = 0;

    public void addObserver(GUI<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(GUI<?> observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        if (!BankingPlugin.getInstance().isEnabled())
            return;
        queuedUpdates++;
        Utils.runTaskLater(this::executeIfLast, 1);
    }

    private void executeIfLast() {
        if (queuedUpdates-- > 1)
            return;
        observers.forEach(GUI::update);
    }

}
