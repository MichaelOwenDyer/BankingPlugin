package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MaterialTransformer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of {@link Material}s that are blacklisted from generating value in an account.
 */
public class Blacklist extends ConfigurationValue<Set<Material>> {
    
    private final EnumSet<Material> materials = EnumSet.allOf(Material.class);

    public Blacklist(BankingPlugin plugin) {
        super(plugin, "blacklist", EnumSet.noneOf(Material.class),
                new MaterialTransformer().collect(() -> EnumSet.noneOf(Material.class)));
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return materials.stream()
                .map(Material::name)
                .filter(name -> name.startsWith(args[args.length - 1].toUpperCase()))
                .collect(Collectors.toList());
    }

    public boolean contains(Material item) {
        return get().contains(item);
    }

}
