package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.Gui;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    Set<Gui<?>> observers = new HashSet<>();
    boolean scheduled = false;
    BukkitRunnable notifyTimer = Utils.bukkitRunnable(() -> observers.forEach(Gui::update));

    public void addObserver(Gui<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(Gui<?> observer) {
        observers.remove(observer);
    }

    protected void notifyObservers() {
        if (scheduled)
            notifyTimer.cancel();
        notifyTimer.runTaskLater(BankingPlugin.getInstance(), 10);
        scheduled = true;
    }

}
