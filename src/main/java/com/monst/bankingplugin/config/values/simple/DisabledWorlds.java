package com.monst.bankingplugin.config.values.simple;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigSet<World> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet());
    }

    private static Set<World> getWorldSet(MemorySection config, String path) {
        return config.getStringList(path).stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<World> readValueFromFile(MemoryConfiguration config, String path) {
        return config.getStringList(path).stream()
                .map(this::parseSingle)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public World parseSingle(String input) {
        return Bukkit.getWorld(input);
    }

}
