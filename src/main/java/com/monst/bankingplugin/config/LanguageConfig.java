package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Messages;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageConfig extends FileConfiguration {

    private final BankingPlugin plugin;
    private final Path langFolder;
    private final Path defaultLangFile;
    private Path currentFile;
    private Map<String, String> filePathValues;

    public LanguageConfig(BankingPlugin plugin) {
        this.plugin = plugin;
        this.filePathValues = new HashMap<>();
        this.langFolder = plugin.getDataFolder().toPath().resolve("lang");
        this.defaultLangFile = langFolder.resolve("en_US.lang");
    }

    public void reload() {
        Path languageFile = langFolder.resolve(Config.languageFile.get());

        if (!Files.exists(defaultLangFile))
            plugin.saveResource("lang/en_US.lang", false);

        if (Files.exists(languageFile)) {
            try {
                load(languageFile);
                currentFile = languageFile;
                plugin.getLogger().info("Using locale \"" + removeExtension(languageFile) + "\"");
            } catch (IOException e) {
                plugin.getLogger().warning("Using default language values.");
                plugin.debug("Using default language values (#1)");
                plugin.debug(e);
            }
        } else {
            try {
                load(defaultLangFile);
                currentFile = defaultLangFile;
                plugin.getLogger().info("Using locale \"en_US\"");
            } catch (IOException e) {
                streamFromJar();
            }
        }
        for (Message message : Message.values())
            Messages.setTranslation(message, findTranslation(message));
    }

    @Nonnull
    private String findTranslation(@Nonnull Message message) {
        String messagePath = message.getPath();
        String translation = getString(messagePath);
        if (translation != null)
            return translation;
        // Value was missing
        String defaultMessage = message.getDefaultMessage();
        filePathValues.put(messagePath, defaultMessage);
        if (currentFile != null && Files.exists(currentFile)) {
            // Append missing entry to loaded language file
            try (BufferedWriter writer = Files.newBufferedWriter(currentFile, StandardOpenOption.APPEND)) {
                writer.write("\n# Scenario: " + message.getDescription());
                writer.write("\n# Available placeholders: " + message.getFormattedPlaceholdersList());
                writer.write("\n" + messagePath + "=" + defaultMessage + "\n");
                plugin.getLogger().info("Missing translation for \"" + messagePath + "\" has been added as \"" +
                        defaultMessage + "\" to the selected language file.");
            } catch (IOException e) {
                plugin.debug("Failed to add language entry");
                plugin.debug(e);
                plugin.getLogger().severe("Failed to add missing translation for \"" + messagePath + "\".");
            }
        }
        return defaultMessage;
    }

    @Override
    public String getString(@Nonnull String path) {
        return filePathValues.get(path);
    }

    public void load(Path path) throws IOException {
        readInLines(Files.lines(path));
    }

    private void readInLines(@Nonnull Stream<String> lines) {
        filePathValues = lines
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .filter(l -> l.contains("="))
                .map(l -> l.split("=", 2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1], (existing, replacement) -> replacement));
    }

    @Override
    public void loadFromString(@Nonnull String s) {
        readInLines(Arrays.stream(s.split("\n")));
    }

    @Override
    @Nonnull
    public String saveToString() {
        return filePathValues.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    @Nonnull
    protected String buildHeader() {
        return "";
    }

    private void streamFromJar() {
        Reader reader = plugin.getTextResourceMirror(defaultLangFile.toString());
        if (reader != null) {
            try (BufferedReader br = new BufferedReader(reader)) {
                readInLines(br.lines());
                plugin.getLogger().info("Using lang file \"" + defaultLangFile.getFileName() + "\" (Streamed from .jar)");
            } catch (IOException e) {
                plugin.getLogger().warning("Using default language values.");
                plugin.debug("Using default language values (#3)");
            }
        } else {
            plugin.getLogger().warning("Using default language values.");
            plugin.debug("Using default language values (#4, Reader is null)");
        }
    }

    private static String removeExtension(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - 5);
    }

}
