package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.PathParseException;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseFile extends ConfigValue<String, Path> implements NonNativeString<Path> {

    public DatabaseFile() {
        super("database-file", Paths.get("banking.db"));
    }

    @Override
    public Path parse(String input) throws PathParseException {
        return Parser.parsePath(input, ".db");
    }

    @Override
    void afterSet() {
        PLUGIN.reloadEntities(Callback.doNothing());
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(getFormatted());
        tabCompletions.accept("banking");
        Path databaseFolder = PLUGIN.getDataFolder().toPath().resolve("database");
        try {
            Files.walk(databaseFolder, 1)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".db"))
                    .map(fileName -> fileName.substring(0, fileName.length() - 3)) // Remove extension
                    .forEach(tabCompletions);
        } catch (IOException ignored) {}
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

    @Override
    public Object convertToStorableType(Path path) {
        String fileName = path.toString();
        return fileName.substring(0, fileName.length() - 3); // Remove extension
    }

}
