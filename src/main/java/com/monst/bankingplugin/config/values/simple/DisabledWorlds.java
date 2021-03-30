package com.monst.bankingplugin.config.values.simple;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DisabledWorlds extends SimpleSet<World> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet(), DisabledWorlds::getWorldSet);
    }

    private static Set<World> getWorldSet(MemorySection config, String path) {
        return config.getStringList(path).stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    Stream<World> parseToStream(String input) {
        return Arrays.stream(input.split("\\s*,\\s"))
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull);
    }

}
