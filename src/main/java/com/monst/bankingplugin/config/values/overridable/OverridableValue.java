package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.Overridable;
import com.monst.bankingplugin.config.values.simple.AllowOverride;
import com.monst.bankingplugin.exceptions.ArgumentParseException;

public abstract class OverridableValue<T> extends ConfigValue<T> implements Overridable<T> {

    private final AllowOverride allowOverride;

    OverridableValue(String path, T prescribedValue) {
        super(path + ".default", prescribedValue);
        this.allowOverride = new AllowOverride(path);
    }

    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }

    public T getDefault() {
        return get();
    }

    @Override
    public void parseAndSet(String input) throws ArgumentParseException {
        super.parseAndSet(input);
        notifyGUIs();
    }

    @Override
    public final OverriddenValue<T> override(T value) {
        return new OverriddenValue<>(this, value);
    }

    private static void notifyGUIs() {
        BankingPlugin.getInstance().getBankRepository().getAll().forEach(Bank::notifyObservers);
    }

}
