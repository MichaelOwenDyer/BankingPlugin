package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    final Set<GUI<?>> observers = new HashSet<>();
    boolean cooldown = false;

    public void addObserver(GUI<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(GUI<?> observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        BankingPlugin plugin = BankingPlugin.getInstance();
        if (!plugin.isEnabled())
            return;
        if (cooldown) {
            plugin.debug(new IllegalStateException("GUIs were updated too quickly in succession!"));
            return;
        }
        BankingPlugin.runTaskLater(() -> cooldown = false, 1);
        observers.forEach(GUI::update);
        cooldown = true;
    }

}
