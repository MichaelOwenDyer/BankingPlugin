package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Utils;
import com.monst.pluginconfiguration.ConfigurationCollection;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigurationCollection<World, Set<World>> {

    private final BankingPlugin plugin;

    public DisabledWorlds(BankingPlugin plugin) {
        super(plugin, "disabled-worlds", Collections.emptySet());
        this.plugin = plugin;
    }

    @Override
    public Set<World> createCollection() {
        return new HashSet<>();
    }

    @Override
    public World parseElement(String input) throws ArgumentParseException {
        return Optional.ofNullable(input).map(Bukkit::getWorld).orElseThrow(
                () -> new ArgumentParseException(Message.NOT_A_WORLD.with(Placeholder.INPUT).as(input).translate(plugin)));
    }

    @Override
    public String formatElement(World world) {
        return world.getName();
    }

    @Override
    protected Object convertToFileData(Set<World> worlds) {
        return worlds.stream().map(World::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        List<String> argsList = Arrays.asList(args);
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !argsList.contains(name))
                .filter(name -> Utils.containsIgnoreCase(name, args[args.length - 1]))
                .collect(Collectors.toList());
    }

    public boolean contains(World world) {
        return get().contains(world);
    }

}
