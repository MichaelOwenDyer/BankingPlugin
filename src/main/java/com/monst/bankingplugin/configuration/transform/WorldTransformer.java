package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldTransformer implements Transformer<World> {
    
    @Override
    public World parse(String input) throws ArgumentParseException {
        World world = Bukkit.getWorld(input);
        if (world == null)
            throw new ArgumentParseException(Message.NOT_A_WORLD.with(Placeholder.INPUT).as(input));
        return world;
    }
    
    @Override
    public Object toYaml(World value) {
        return value.getName();
    }
    
    @Override
    public String format(World value) {
        return value.getName();
    }
    
}
