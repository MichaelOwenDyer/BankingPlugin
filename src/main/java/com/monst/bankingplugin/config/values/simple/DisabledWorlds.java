package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.WorldParseException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DisabledWorlds extends SimpleSet<World> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet());
    }

    @Override
    public Set<World> readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        Set<World> worlds = new HashSet<>();
        for (String world : config.getStringList(path))
            try {
                worlds.add(parseSingle(world));
            } catch (WorldParseException ignored) {}
        return worlds;
    }

    @Override
    public World parseSingle(String input) throws WorldParseException {
        return Optional.ofNullable(Bukkit.getWorld(input)).orElseThrow(() -> new WorldParseException(input));
    }

}
