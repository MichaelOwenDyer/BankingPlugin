package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigList;

import java.util.List;

public abstract class ConfigList<T> extends ConfigValue<List<T>> implements IConfigList<T> {

    public ConfigList(String path, List<T> defaultValue) {
        super(path, defaultValue);
    }

}
