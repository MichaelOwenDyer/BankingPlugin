package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

public abstract class SimpleConfigValue<T> extends ConfigValue<T> {

    String path;

    public SimpleConfigValue(String path, T defaultValue, BiFunction<FileConfiguration, String, T> valueFinder) {
        super(defaultValue, valueFinder);
        this.path = path;
    }

    @Override
    public T get() {
        return super.get();
    }

    @Override
    protected String getPath() {
        return path;
    }

//    @Override
//    public void set(String input) throws ArgumentParseException {
//        CONFIG.set(getPath(), parse(input));
//    }

    abstract T parse(String input) throws ArgumentParseException;

    @Override
    public void clear() {
        lastSeenValue = null;
    }

}
