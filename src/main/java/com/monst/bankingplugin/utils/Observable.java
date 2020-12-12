package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    protected static final BankingPlugin plugin = BankingPlugin.getInstance();
    Set<GUI<?>> observers = new HashSet<>();
    BukkitRunnable notifyTimer;
    boolean scheduled = false;

    public void addObserver(GUI<?> observer) {
        observers.add(observer);
    }

    public void removeObserver(GUI<?> observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        if (!plugin.isEnabled())
            return;
        if (scheduled)
            notifyTimer.cancel();
        notifyTimer = Utils.bukkitRunnable(() -> {
            observers.forEach(GUI::update);
            scheduled = false;
        });
        notifyTimer.runTaskLater(plugin, 1);
        scheduled = true;
    }

}
