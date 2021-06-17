package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;

import java.util.function.Consumer;

public abstract class Callback<T> {

    public static <T> Callback<T> doNothing() {
        return of(result -> {}); // do nothing
    }

    public static <T> Callback<T> of(Consumer<T> onResult) {
        return new Callback<T>() {
            @Override
            public void onResult(T result) {
                onResult.accept(result);
            }
        };
    }

    public static <T> Callback<T> of(Consumer<T> onResult, Consumer<Throwable> onError) {
        return new Callback<T>() {
            @Override
            public void onResult(T result) {
                onResult.accept(result);
            }
            @Override
            public void onError(Throwable throwable) {
                PLUGIN.debug(throwable);
                onError.accept(throwable);
            }
        };
    }

	private static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

    private Callback() {}

    public void onResult(T result) {}

    public void onError(Throwable throwable) {
        PLUGIN.debug(throwable);
    }

    /**
     * Returns a null result on the current thread if the callback is not null.
     */
    public static void callResult(Callback<?> callback) {
        Callback.callResult(callback, null);
    }

    /**
     * Returns the specified result on the current thread if the callback is not null.
     */
    public static <T> void callResult(Callback<T> callback, T result) {
        if (callback != null)
            callback.onResult(result);
    }

    /**
     * Returns a null result <i>on the main thread</i> if the callback is not null.
     * This should only be called from an asynchronous context, otherwise a task is created unnecessarily.
     */
    public static void callSyncResult(Callback<?> callback) {
        Callback.callSyncResult(callback, null);
    }

    /**
     * Returns the specified result <i>on the main thread</i> if the callback is not null.
     * This should only be called from an asynchronous context, otherwise a task is created unnecessarily.
     */
    public static <T> void callSyncResult(Callback<T> callback, T result) {
        if (callback != null)
            Utils.bukkitRunnable(() -> callback.onResult(result)).runTask(PLUGIN);
    }

}
