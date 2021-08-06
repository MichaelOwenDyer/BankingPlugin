package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

    public boolean contains(ItemStack item) {
        if (item == null)
            return true; // Return true so that no attempt at valuing a null item is made
        return get().contains(item.getType());
    }

}
