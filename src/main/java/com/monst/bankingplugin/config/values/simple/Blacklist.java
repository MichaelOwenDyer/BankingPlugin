package com.monst.bankingplugin.config.values.simple;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blacklist extends SimpleSet<Material> {

    public Blacklist() {
        super("blacklist", EnumSet.noneOf(Material.class), Blacklist::getMaterialSet);
    }

    private static EnumSet<Material> getMaterialSet(MemorySection config, String path) {
        return config.getStringList(path).stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
    }

    @Override
    Stream<Material> parseToStream(String input) {
        return Arrays.stream(input.split("\\s*,\\s*"))
                .map(Material::getMaterial)
                .filter(Objects::nonNull);
    }

}
