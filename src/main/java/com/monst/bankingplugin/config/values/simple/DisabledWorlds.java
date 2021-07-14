package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.parse.WorldParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.World;

import java.util.Collections;

public class DisabledWorlds extends SimpleSet<World> {

    public DisabledWorlds() {
        super("disabled-worlds", Collections.emptySet());
    }

    @Override
    public World parseSingle(String input) throws WorldParseException {
        return Parser.parseWorld(input);
    }

}
