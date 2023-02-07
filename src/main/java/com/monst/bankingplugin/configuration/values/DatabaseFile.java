package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.PathConfigurationValue;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseFile extends PathConfigurationValue {

    private final Path databaseFolder;

    public DatabaseFile(BankingPlugin plugin) {
        super(plugin, "database-file", Paths.get("banking"));
        this.databaseFolder = plugin.getDataFolder().toPath().resolve("database");
    }

    @Override
    protected void afterSet() {
        plugin.reloadDatabase();
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(toString());
        tabCompletions.accept("banking");
        try (Stream<Path> children = Files.walk(databaseFolder, 1)) {
            children.filter(Files::isDirectory)
                    .map(databaseFolder::relativize)
                    .map(Path::toString)
                    .forEach(tabCompletions);
        } catch (IOException e) {
            plugin.debug(e);
        }
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

    public String getJdbcUrl() {
        return "jdbc:hsqldb:file:" + databaseFolder.resolve(get()).resolve(get().getFileName());
    }

}
