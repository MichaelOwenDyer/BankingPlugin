package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    protected final BankingPlugin plugin;
    private final Set<GUI<?>> observers;
    private int queuedUpdates = 0;

    public Observable(BankingPlugin plugin) {
        this.plugin = plugin;
        observers = new HashSet<>();
    }

    public void addObserver(GUI<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(GUI<?> observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        if (!plugin.isEnabled())
            return;
        queuedUpdates++;
        plugin.runLater(this::executeIfLast, 1);
    }

    private void executeIfLast() {
        if (queuedUpdates-- > 1)
            return;
        observers.forEach(GUI::update);
    }

}
