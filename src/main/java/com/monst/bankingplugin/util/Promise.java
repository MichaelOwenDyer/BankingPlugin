package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface Promise<T> {
    
    Promise<T> then(Consumer<T> onResult);
    
    Promise<T> catchError(Consumer<Exception> onError);
    
    Promise<T> finallyDo(Runnable runnable);
    
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
            
            private final Object lock = new Object();
            
            {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        T localResult = task.call();
                        synchronized (lock) {
                            this.result = localResult;
                            if (onFulfilled != null)
                                Bukkit.getScheduler().runTask(plugin, () -> onFulfilled.accept(result));
                        }
                    } catch (Exception e) {
                        synchronized (lock) {
                            this.exception = e;
                            if (onRejected != null)
                                Bukkit.getScheduler().runTask(plugin, () -> onRejected.accept(exception));
                        }
                        plugin.debug(exception);
                    } finally {
                        synchronized (lock) {
                            if (finallyDo != null)
                                Bukkit.getScheduler().runTask(plugin, finallyDo);
                        }
                    }
                });
            }
            
            @Override
            public Promise<T> then(Consumer<T> onFulfilled) {
                synchronized (lock) {
                    if (result != null)
                        onFulfilled.accept(result);
                    else
                        this.onFulfilled = onFulfilled;
                }
                return this;
            }
    
            @Override
            public Promise<T> catchError(Consumer<Exception> onRejected) {
                synchronized (lock) {
                    if (exception != null)
                        onRejected.accept(exception);
                    else
                        this.onRejected = onRejected;
                }
                return this;
            }
            
            @Override
            public Promise<T> finallyDo(Runnable finallyDo) {
                synchronized (lock) {
                    if (result != null || exception != null)
                        finallyDo.run();
                    else
                        this.finallyDo = finallyDo;
                }
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
            public Promise<T> catchError(Consumer<Exception> onError) {
                return this;
            }
    
            @Override
            public Promise<T> finallyDo(Runnable finallyDo) {
                finallyDo.run();
                return this;
            }
        };
    }
    
}
