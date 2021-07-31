package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import org.bukkit.command.CommandSender;

abstract class OverridableValue<V, T> extends ConfigValue<V, T> {

    private final AllowOverride allowOverride;

    OverridableValue(BankingPlugin plugin, String path, T defaultValue) {
        super(plugin, path + ".default", defaultValue);
        this.allowOverride = new AllowOverride(plugin, path);
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
    public void afterSet(CommandSender executor) {
        plugin.getBankRepository().getAll().forEach(Bank::notifyObservers);
    }

    public final OverriddenValue<T> override(T value) {
        return new OverriddenValue<>(this, value);
    }

}
