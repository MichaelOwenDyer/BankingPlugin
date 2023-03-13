package com.monst.bankingplugin.configuration;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;
import com.monst.bankingplugin.entity.Bank;

/**
 * Represents a configuration value that can be set globally and overridden per bank.
 * If the value is overridden, a bank will use the overridden value unless {@link #allowOverride} is set to false.
 * If the value is not overridden or {@link #allowOverride} is set to false, a bank will use the global value.
 * @param <T> The type of the value
 */
public abstract class ConfigurationPolicy<T> extends ConfigurationBranch {
    
    private static class AllowOverride extends ConfigurationValue<Boolean> {
        private AllowOverride(BankingPlugin plugin) {
            super(plugin, "allow-override", true, new BooleanTransformer());
        }
    }
    
    private final BankingPlugin plugin;
    private final ConfigurationValue<T> global;
    private final ConfigurationValue<Boolean> allowOverride;
    
    public ConfigurationPolicy(BankingPlugin plugin, String key, ConfigurationValue<T> global) {
        super(key);
        this.plugin = plugin;
        this.global = addChild(global);
        this.allowOverride = addChild(new AllowOverride(plugin));
    }
    
    public T at(Bank bank) {
        if (get(bank) != null) {
            if (allowOverride.get())
                return get(bank);
            return global.get();
        }
        if (plugin.config().stickyDefaults.get())
            set(bank, global.get());
        return global.get();
    }
    
    public void parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            if (plugin.config().stickyDefaults.get())
                set(bank, global.get());
            else
                set(bank, null);
            return;
        }
        T value = global.getTransformer().parse(input);
        set(bank, value);
    }
    
    public String toStringAt(Bank bank) {
        T value = get(bank);
        if (value == null)
           return global.toString();
        return global.getTransformer().format(value);
    }
    
    public boolean isOverridable() {
        return allowOverride.get();
    }
    
    protected abstract T get(Bank bank);
    
    protected abstract void set(Bank bank, T value);
    
}
