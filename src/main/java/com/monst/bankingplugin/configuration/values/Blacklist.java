package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.ConfigurationCollection;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A collection of {@link Material}s that are blacklisted from generating value in an account.
 */
public class Blacklist extends ConfigurationCollection<Material, Set<Material>> {

    public Blacklist(BankingPlugin plugin) {
        super(plugin, "blacklist", EnumSet.noneOf(Material.class));
    }

    @Override
    public Set<Material> createCollection() {
        return EnumSet.noneOf(Material.class);
    }

    @Override
    public Material parseElement(String input) throws ArgumentParseException {
        return Optional.ofNullable(input)
                .map(Material::matchMaterial)
                .orElseThrow(() -> new ArgumentParseException(Message.NOT_A_MATERIAL.with(Placeholder.INPUT).as(input)));
    }

    @Override
    protected Object convertToYamlType(Set<Material> materials) {
        return materials.stream().map(Material::name).collect(Collectors.toList());
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        return super.getTabCompletions(player, args);
    }

    public boolean contains(Material item) {
        return get().contains(item);
    }

}
