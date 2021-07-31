package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Blacklist extends ConfigValue<List<String>, Set<Material>> implements ConfigCollection<Material, Set<Material>> {

    public Blacklist(BankingPlugin plugin) {
        super(plugin, "blacklist", EnumSet.noneOf(Material.class));
    }

    @Override
    public Set<Material> getEmptyCollection() {
        return EnumSet.noneOf(Material.class);
    }

    @Override
    public Material parseSingle(String input) throws MaterialParseException {
        return Parser.parseMaterial(input);
    }

    @Override
    public Object convertToStorableType(Set<Material> set) {
        return set.stream().map(Material::toString).collect(Collectors.toList()); // must cast to List<String> in order to set
    }

    public boolean contains(ItemStack item) {
        if (item == null)
            return true; // Return true so that no attempt at valuing a null item is made
        return get().contains(item.getType());
    }

}
