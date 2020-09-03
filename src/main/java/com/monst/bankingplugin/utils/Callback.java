package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public abstract class Callback<T> {

    public static <T> Callback<T> of(BankingPlugin plugin, Consumer<T> onResult) {
        return new Callback<T>(plugin) {
            @Override
            public void onResult(T result) {
                onResult.accept(result);
            }
        };
    }

    public static <T> Callback<T> of(BankingPlugin plugin, Consumer<T> onResult, Consumer<Throwable> onError) {
        return new Callback<T>(plugin) {
            @Override
            public void onResult(T result) {
                onResult.accept(result);
            }
            @Override
            public void onError(Throwable throwable) {
                plugin.debug(throwable);
                onError.accept(throwable);
            }
        };
    }

	private final BankingPlugin plugin;

    private Callback(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public void onResult(T result) {}

    public void onError(Throwable throwable) {
        plugin.debug(throwable);
    }

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
