package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.Gui;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    protected static final BankingPlugin plugin = BankingPlugin.getInstance();
    Set<Gui<?>> observers = new HashSet<>();
    BukkitRunnable notifyTimer;
    boolean scheduled = false;

    public void addObserver(Gui<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(Gui<?> observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        if (!plugin.isEnabled())
            return;
        if (scheduled)
            notifyTimer.cancel();
        notifyTimer = Utils.bukkitRunnable(() -> {
            observers.forEach(Gui::update);
            scheduled = false;
        });
        notifyTimer.runTaskLater(plugin, 1);
        scheduled = true;
    }

}
