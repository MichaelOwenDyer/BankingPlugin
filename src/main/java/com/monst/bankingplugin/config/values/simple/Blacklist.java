package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class Blacklist extends SimpleSet<Material> {

    public Blacklist() {
        super("blacklist", EnumSet.noneOf(Material.class));
    }

    @Override
    public Set<Material> getEmptyCollection() {
        return EnumSet.noneOf(Material.class);
    }

    @Override
    public Material parseSingle(String input) throws MaterialParseException {
        return Optional.ofNullable(Material.getMaterial(input)).orElseThrow(() -> new MaterialParseException(input));
    }

}
