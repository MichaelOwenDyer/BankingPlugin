package com.monst.bankingplugin.config.values;

import org.bukkit.configuration.MemoryConfiguration;

interface NativeString extends NativeValue<String> {

    @Override
    default String parse(String input) {
        return input;
    }

    @Override
    default Object get(MemoryConfiguration config, String path) {
        return config.getString(path, null);
    }

    @Override
    default String cast(Object o) {
        return (String) o;
    }

}
