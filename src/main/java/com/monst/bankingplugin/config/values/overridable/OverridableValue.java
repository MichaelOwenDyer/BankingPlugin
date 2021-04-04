package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public abstract class OverridableValue<T> extends ConfigValue<T> {

    final String defaultPath;
    final String allowOverridePath;
    Boolean lastSeenOverridableValue;

    OverridableValue(String path, T defaultValue, BiFunction<FileConfiguration, String, T> valueFinder) {
        super(defaultValue, valueFinder);
        this.defaultPath = path + ".default";
        this.allowOverridePath = path + ".allow-override";
    }

    @Override
    protected String getPath() {
        return defaultPath;
    }

    @Override
    public List<String> getPaths() {
        return Arrays.asList(defaultPath, allowOverridePath);
    }

    public T getDefault() {
        return super.get();
    }

    @Override
    public void set(String path, String input) throws ArgumentParseException {
        super.set(path, input);
        BankingPlugin.getInstance().getBankRepository().getAll().forEach(Bank::notifyObservers);
    }

    public boolean isOverridable() {
        if (lastSeenOverridableValue == null)
            lastSeenOverridableValue = CONFIG.getBoolean(allowOverridePath, true);
        return lastSeenOverridableValue;
    }

    public final OverriddenValue<T> override(T value) {
        return new OverriddenValue<>(this, value);
    }

    @Override
    public void clear() {
        lastSeenValue = null;
        lastSeenOverridableValue = null;
    }

}
