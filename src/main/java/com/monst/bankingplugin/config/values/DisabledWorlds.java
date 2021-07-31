package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.WorldParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class DisabledWorlds extends ConfigValue<List<String>, Set<World>> implements ConfigCollection<World, Set<World>> {

    public DisabledWorlds(BankingPlugin plugin) {
        super(plugin, "disabled-worlds", Collections.emptySet());
    }

    @Override
    public Set<World> getEmptyCollection() {
        return new HashSet<>();
    }

    @Override
    public World parseSingle(String input) throws WorldParseException {
        return Parser.parseWorld(input);
    }

    @Override
    public Object convertToStorableType(Set<World> set) {
        return set.stream().map(World::getName).collect(Collectors.toList()); // must cast to List<String> in order to set
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> argsList = Arrays.asList(args);
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !argsList.contains(name))
                .filter(name -> Utils.containsIgnoreCase(name, args[args.length - 1]))
                .collect(Collectors.toList());
    }

}
