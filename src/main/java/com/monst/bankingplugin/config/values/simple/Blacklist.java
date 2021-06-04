package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.MaterialParseException;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class Blacklist extends SimpleSet<Material> {

    public Blacklist() {
        super("blacklist", EnumSet.noneOf(Material.class));
    }

    @Override
    public Set<Material> readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        for (String material : config.getStringList(path))
            try {
                materials.add(parseSingle(material));
            } catch (MaterialParseException ignored) {}
        return materials;
    }

    @Override
    public Material parseSingle(String input) throws MaterialParseException {
        return Optional.ofNullable(Material.getMaterial(input)).orElseThrow(() -> new MaterialParseException(input));
    }

}
