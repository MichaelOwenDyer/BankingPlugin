package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import org.bukkit.command.CommandSender;

abstract class OverridableValue<V, T> extends ConfigValue<V, T> {

    public final AllowOverride allowOverride;

    OverridableValue(BankingPlugin plugin, String path, T defaultValue) {
        super(plugin, path + ".default", defaultValue);
        this.allowOverride = new AllowOverride(plugin, path);
    }

    boolean isOverridable() {
        return allowOverride.get();
    }

    @Override
    public void afterSet(CommandSender executor) {
        plugin.getBankRepository().getAll().forEach(Bank::notifyObservers);
    }

    public final OverriddenValue<T> override(T value) {
        return new OverriddenValue<>(this, value);
    }

    private static class AllowOverride extends ConfigValue<Boolean, Boolean> implements NativeBoolean {
        private AllowOverride(BankingPlugin plugin, String path) {
            super(plugin, path + ".allow-override", true);
        }
    }

}
