package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

abstract class OverridableValue<T> extends ConfigValue<T> {

    String defaultPath;
    String allowOverridePath;
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

    public T getDefault() {
        return super.get();
    }

    public boolean isOverridable() {
        if (lastSeenOverridableValue == null)
            lastSeenOverridableValue = CONFIG.getBoolean(allowOverridePath, true);
        return lastSeenOverridableValue;
    }

    public abstract OverriddenValue<T> override(T value);

    @Override
    public void clear() {
        lastSeenValue = null;
        lastSeenOverridableValue = null;
    }

}
