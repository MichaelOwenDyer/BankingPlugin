package com.monst.bankingplugin.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Converter
public class WorldConverter implements AttributeConverter<World, String> {
    @Override
    public String convertToDatabaseColumn(World world) {
        return world.getName();
    }

    @Override
    public World convertToEntityAttribute(String worldName) {
        return Bukkit.getWorld(worldName);
    }
}
