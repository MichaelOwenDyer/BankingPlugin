package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;

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

    public static void yield(Callback<?> callback) {
        Callback.yield(callback, null);
    }

    public static <T> void yield(Callback<T> callback, T result) {
        if (callback != null)
            callback.yield(result);
    }

    public static void error(Callback<?> callback, Throwable error) {
        if (callback != null)
            callback.error(error);
    }

    protected final void yield(final T result) {
        Utils.bukkitRunnable(() -> onResult(result)).runTask(plugin);
    }

    public final void error(final Throwable throwable) {
        Utils.bukkitRunnable(() -> onError(throwable)).runTask(plugin);
    }

    public Callback<T> andThen(Consumer<T> nextAction) {
        return of(plugin, result -> {
            onResult(result);
            nextAction.accept(result);
        }, this::onError);
    }

    public Callback<T> andThen(Consumer<T> nextAction, Consumer<Throwable> nextOnError) {
        return of(plugin, result -> {
            onResult(result);
            nextAction.accept(result);
        }, throwable -> {
            onError(throwable);
            nextOnError.accept(throwable);
        });
    }
}
