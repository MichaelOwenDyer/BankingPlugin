package com.monst.bankingplugin.config.values.simple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageFile extends SimpleString {

    public LanguageFile() {
        super("language-file", "en_US");
    }

    @Override
    protected void afterSet(String newValue) {
        PLUGIN.getLanguageConfig().reload();
    }

    @Override
    public List<String> getTabCompletions() {
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
