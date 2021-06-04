package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.WorldParseException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Collections;
import java.util.Optional;

public class DisabledWorlds extends SimpleSet<World> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet());
    }

    @Override
    public World parseSingle(String input) throws WorldParseException {
        return Optional.ofNullable(Bukkit.getWorld(input)).orElseThrow(() -> new WorldParseException(input));
    }

}
