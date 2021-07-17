package com.monst.bankingplugin.config.values;

public abstract class UnaryConfigValue<T> extends ConfigValue<T, T> implements IUnaryConfigValue<T> {

    protected UnaryConfigValue(String path, T defaultConfiguration) {
        super(path, defaultConfiguration);
    }

}
