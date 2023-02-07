package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface Promise<T> {
    
    default Promise<T> then(Consumer<T> onResult) {
        return this;
    }
    
    default Promise<T> catchError(Consumer<Exception> onError) {
        return this;
    }
    
    default Promise<T> finallyDo(Runnable runnable) {
        return this;
    }
    
    // TODO: Add Plugin parameter to sync() method?
    static <T> Promise<T> sync(Callable<T> task) {
        return new Promise<T>() {
            
            private T result;
            private Exception exception;
            
            {
                try {
                    this.result = task.call();
                } catch (Exception e) {
                    this.exception = e;
                }
            }
            
            @Override
            public Promise<T> then(Consumer<T> onFulfilled) {
                onFulfilled.accept(result);
                return this;
            }
    
            @Override
            public Promise<T> catchError(Consumer<Exception> onRejected) {
                onRejected.accept(exception);
                return this;
            }
            
            @Override
            public Promise<T> finallyDo(Runnable runnable) {
                runnable.run();
                return this;
            }
        };
    }
    
    static <T> Promise<T> async(BankingPlugin plugin, Callable<T> task) {
        return new Promise<T>() {
            
            private T result;
            private Exception exception;
            
            private Consumer<T> onFulfilled;
            private Consumer<Exception> onRejected;
            private Runnable finallyDo;
            
            {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        this.result = task.call();
                        if (onFulfilled != null)
                            Bukkit.getScheduler().runTask(plugin, () -> onFulfilled.accept(result));
                    } catch (Exception e) {
                        this.exception = e;
                        plugin.debug(exception);
                        if (onRejected != null)
                            Bukkit.getScheduler().runTask(plugin, () -> onRejected.accept(exception));
                    } finally {
                        if (finallyDo != null)
                            Bukkit.getScheduler().runTask(plugin, finallyDo);
                    }
                });
            }
            
            @Override
            public Promise<T> then(Consumer<T> onFulfilled) {
                if (result == null)
                    this.onFulfilled = onFulfilled;
                else
                    onFulfilled.accept(result);
                return this;
            }
    
            @Override
            public Promise<T> catchError(Consumer<Exception> onRejected) {
                if (exception == null)
                    this.onRejected = onRejected;
                else
                    onRejected.accept(exception);
                return this;
            }
            
            @Override
            public Promise<T> finallyDo(Runnable finallyDo) {
                if (result == null && exception == null)
                    this.finallyDo = finallyDo;
                else
                    finallyDo.run();
                return this;
            }
        };
    }
    
    static <T> Promise<T> fulfill(T t) {
        return new Promise<T>() {
            @Override
            public Promise<T> then(Consumer<T> onFulfilled) {
                onFulfilled.accept(t);
                return this;
            }
            
            @Override
            public Promise<T> finallyDo(Runnable finallyDo) {
                finallyDo.run();
                return this;
            }
        };
    }
    
    static <T> Promise<T> reject(Exception e) {
        return new Promise<T>() {
            @Override
            public Promise<T> catchError(Consumer<Exception> onRejected) {
                onRejected.accept(e);
                return this;
            }
            
            @Override
            public Promise<T> finallyDo(Runnable runnable) {
                runnable.run();
                return this;
            }
        };
    }
    
    static <T> Promise<T> empty() {
        return new Promise<T>() {};
    }
    
}
