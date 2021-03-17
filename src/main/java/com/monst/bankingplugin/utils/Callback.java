package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;

import java.util.function.Consumer;

public abstract class Callback<T> {

    public static <T> Callback<T> blank() {
        return of(t -> {});
    }

    public static <T> Callback<T> blankNoLog() {
        return of(t -> {}, e -> {});
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

    final void yield(final T result) {
        BankingPlugin.runTask(() -> onResult(result));
    }

    public final void error(final Throwable throwable) {
        BankingPlugin.runTask(() -> onError(throwable));
    }

    public Callback<T> andThen(Consumer<T> nextAction) {
        return of(result -> {
            onResult(result);
            nextAction.accept(result);
        }, this::onError);
    }

    public Callback<T> andThen(Consumer<T> nextAction, Consumer<Throwable> nextOnError) {
        return of(result -> {
            onResult(result);
            nextAction.accept(result);
        }, throwable -> {
            onError(throwable);
            nextOnError.accept(throwable);
        });
    }
}
