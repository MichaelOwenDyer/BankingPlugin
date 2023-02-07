package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.ConfigurationCollection;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigurationCollection<World, Set<World>> {

    public DisabledWorlds(BankingPlugin plugin) {
        super(plugin, "disabled-worlds", Collections.emptySet());
    }

    @Override
    public Set<World> createCollection() {
        return new HashSet<>();
    }

    @Override
    public World parseElement(String input) throws ArgumentParseException {
        return Optional.ofNullable(input)
                .map(Bukkit::getWorld)
                .orElseThrow(() -> new ArgumentParseException(Message.NOT_A_WORLD.with(Placeholder.INPUT).as(input)));
    }

    @Override
    public String formatElement(World world) {
        return world.getName();
    }

    @Override
    protected Object convertToYamlType(Set<World> worlds) {
        return worlds.stream().map(World::getName).collect(Collectors.toList());
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
