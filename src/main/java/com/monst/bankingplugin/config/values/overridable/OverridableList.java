package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.function.BiFunction;

abstract class OverridableList<T> extends OverridableValue<List<T>> {

    OverridableList(String path, List<T> defaultValue, BiFunction<FileConfiguration, String, List<T>> valueFinder) {
        super(path, defaultValue, valueFinder);
    }

}
