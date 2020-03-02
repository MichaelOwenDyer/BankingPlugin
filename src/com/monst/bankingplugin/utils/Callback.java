package com.monst.bankingplugin.utils;

import org.bukkit.scheduler.BukkitRunnable;

import com.monst.bankingplugin.BankingPlugin;

public abstract class Callback<T> {
    private BankingPlugin plugin;

    public Callback(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public void onResult(T result) {}

    public void onError(Throwable throwable) {}

    public final void callSyncResult(final T result) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onResult(result);
            }
        }.runTask(plugin);
    }

    public final void callSyncError(final Throwable throwable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onError(throwable);
            }
        }.runTask(plugin);
    }
}
