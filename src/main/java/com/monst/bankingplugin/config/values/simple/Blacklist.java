package com.monst.bankingplugin.config.values.simple;

import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Blacklist extends ConfigSet<Material> {

    public Blacklist() {
        super("blacklist", EnumSet.noneOf(Material.class));
    }

    @Override
    public Set<Material> readValueFromFile(MemoryConfiguration config, String path) {
        return config.getStringList(path).stream()
                .map(this::parseSingle)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
    }

    @Override
    public Material parseSingle(String input) {
        return Material.getMaterial(input);
    }

}
