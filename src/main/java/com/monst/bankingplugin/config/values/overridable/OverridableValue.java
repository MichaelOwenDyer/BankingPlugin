package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.simple.AllowOverride;

public abstract class OverridableValue<T> extends ConfigValue<T> {

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
    protected void afterSet(T newValue) {
        PLUGIN.getBankRepository().getAll().forEach(Bank::notifyObservers);
        afterSet2(newValue);
    }

    protected void afterSet2(T newValue) {}

    public OverriddenValue<T> override(T value) {
        return new OverriddenValue<>(this, value);
    }

}
