package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.PathParseException;
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

public class LanguageFile extends ConfigValue<String, Path> implements NonNativeString<Path> {

    public LanguageFile() {
        super("language-file", Paths.get("en_US.lang"));
    }

    @Override
    public Path parse(String input) throws PathParseException {
        return Parser.parsePath(input, ".lang");
    }

    @Override
    void afterSet() {
        PLUGIN.reloadLanguageConfig();
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(getFormatted());
        tabCompletions.accept("en_US");
        Path langFolder = PLUGIN.getDataFolder().toPath().resolve("lang");
        try {
            Files.walk(langFolder, 1)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".lang"))
                    .map(fileName -> fileName.substring(0, fileName.length() - 5)) // Remove extension
                    .forEach(tabCompletions);
        } catch (IOException ignored) {}
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

    @Override
    public Object convertToStorableType(Path path) {
        String fileName = path.toString();
        return fileName.substring(0, fileName.length() - 5); // Remove extension
    }
}
