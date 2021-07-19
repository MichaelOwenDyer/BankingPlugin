package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.WorldParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigValue<List<String>, Set<World>> implements ConfigCollection<World, Set<World>> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet());
    }

    @Override
    public Set<World> getEmptyCollection() {
        return new HashSet<>();
    }

    @Override
    public World parseSingle(String input) throws WorldParseException {
        return Parser.parseWorld(input);
    }

    @Override
    public Object convertToStorableType(Set<World> set) {
        return set.stream().map(World::getName).collect(Collectors.toList()); // must cast to List<String> in order to set
    }

}
