package com.monst.bankingplugin.config.values;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageFile extends ConfigValue<String, String> implements NativeString {

    public LanguageFile() {
        super("language-file", "en_US");
    }

    @Override
    void afterSet(String newValue) {
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
                    .map(fileName -> fileName.replaceFirst("[.][^.]+$", "")) // Remove extension
                    .forEach(tabCompletions::accept);
        } catch (IOException ignored) {}
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

}
