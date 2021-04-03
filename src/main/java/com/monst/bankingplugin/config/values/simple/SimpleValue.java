package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public abstract class SimpleValue<T> extends ConfigValue<T> {

    final String path;

    public SimpleValue(String path, T defaultValue, BiFunction<FileConfiguration, String, T> valueFinder) {
        super(defaultValue, valueFinder);
        this.path = path;
    }

    @Override
    protected String getPath() {
        return path;
    }

    @Override
    public List<String> getPaths() {
        return Collections.singletonList(path);
    }

    @Override
    public void clear() {
        lastSeenValue = null;
    }

}
