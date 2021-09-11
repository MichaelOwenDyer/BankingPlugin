package com.monst.bankingplugin.utils;

import org.bukkit.scheduler.BukkitRunnable;

public class BPRunnable extends BukkitRunnable {
    private final Runnable runnable;
    public BPRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    @Override
    public void run() {
        runnable.run();
    }
}
