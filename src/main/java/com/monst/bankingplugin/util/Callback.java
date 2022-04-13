package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class Callback<T> {

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

    public Callback(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public void onResult(T result) {}

    public void onError(Throwable throwable) {
        plugin.debug(throwable);
    }

    /**
     * Returns the specified result <i>on the main thread</i>.
     * This should only be called from an asynchronous context, otherwise a task is created unnecessarily.
     */
    public void callSyncResult(T result) {
        Bukkit.getScheduler().runTask(plugin, () -> onResult(result));
    }

    /**
     * Returns the specified error <i>on the main thread</i>.
     * This should only be called from an asynchronous context, otherwise a task is created unnecessarily.
     */
    public void callSyncError(String debugMessage, Throwable throwable) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.debug(debugMessage);
            onError(throwable);
        });
    }

}
