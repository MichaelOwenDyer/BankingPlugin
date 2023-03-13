package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Material;

public class MaterialTransformer implements Transformer<Material> {
    
    @Override
    public Material parse(String input) throws ArgumentParseException {
        Material material = Material.matchMaterial(input);
        if (material == null)
            throw new ArgumentParseException(Message.NOT_A_MATERIAL.with(Placeholder.INPUT).as(input));
        return material;
    }
    
    @Override
    public Object toYaml(Material value) {
        return value.name();
    }
    
    @Override
    public String format(Material value) {
        return value.name();
    }
    
}
