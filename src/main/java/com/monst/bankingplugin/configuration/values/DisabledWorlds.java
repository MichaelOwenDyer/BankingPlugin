package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.WorldTransformer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigurationValue<Set<World>> {

    public DisabledWorlds(BankingPlugin plugin) {
        super(plugin, "disabled-worlds", Collections.emptySet(), new WorldTransformer().collect(HashSet::new));
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        List<String> argsList = Arrays.asList(args);
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !argsList.contains(name))
                .filter(name -> name.toLowerCase().contains(args[args.length - 1]))
                .collect(Collectors.toList());
    }

    public boolean contains(World world) {
        return get().contains(world);
    }

}
