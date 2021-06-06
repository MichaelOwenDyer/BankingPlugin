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
        if (!BankingPlugin.getInstance().isEnabled())
            return;
        if (cooldown) {
            BankingPlugin.getInstance().debug("GUIs were updated too quickly in succession!");
            return;
        }
        Utils.runTaskLater(() -> cooldown = false, 1);
        observers.forEach(GUI::update);
        cooldown = true;
    }

}
