package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.banking.Bank;

abstract class OverridableValue<V, T> extends ConfigValue<V, T> {

    private final AllowOverride allowOverride;

    OverridableValue(String path, T defaultValue) {
        super(path + ".default", defaultValue);
        this.allowOverride = new AllowOverride(path);
    }

    public AllowOverride getAllowOverride() {
        return allowOverride;
    }

    boolean isOverridable() {
        return getAllowOverride().get();
    }

    public T getDefault() {
        return get();
    }

    @Override
    void afterSet(T newValue) {
        PLUGIN.getBankRepository().getAll().forEach(Bank::notifyObservers);
    }

    public OverriddenValue<T> override(Bank bank, T value) {
        return new OverriddenValue<>(this, value);
    }

}
