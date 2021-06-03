package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageConfig extends FileConfiguration {

    private final List<String> lines = new ArrayList<>();
    private final HashMap<String, String> configFilePathValues = new HashMap<>();

    private final BankingPlugin plugin;
    private File file;

    public LanguageConfig(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        Path langFolder = plugin.getDataFolder().toPath().resolve("lang");
        Path defaultLangFile = langFolder.resolve("en_US.lang");

        if (!Files.exists(defaultLangFile))
            plugin.saveResource("lang/en_US.lang", false);

        if (!Files.exists(langFolder.resolve("de_DE.lang")))
            plugin.saveResource("lang/de_DE.lang", false);

        Path specifiedLang = langFolder.resolve(Config.languageFile.get() + ".lang");
        if (Files.exists(specifiedLang)) {
            try {
                plugin.getLogger().info("Using locale \"" + Config.languageFile.get() + "\"");
                load(specifiedLang);
            } catch (IOException e) {
                plugin.getLogger().warning("Using default language values.");
                plugin.debug("Using default language values (#1)");
                plugin.debug(e);
            }
        } else {
            if (Files.exists(defaultLangFile)) {
                try {
                    load(defaultLangFile);
                    plugin.getLogger().info("Using locale \"en_US\"");
                } catch (IOException e) {
                    plugin.getLogger().warning("Using default language values.");
                    plugin.debug("Using default language values (#2)");
                    plugin.debug(e);
                }
            } else {
                String fileName;
                Reader reader = plugin.getTextResourceMirror(fileName = specifiedLang.toString());
                if (reader == null)
                    reader = plugin.getTextResourceMirror(fileName = defaultLangFile.toString());

                if (reader != null) {
                    try (BufferedReader br = new BufferedReader(reader)) {
                        loadFromStream(br.lines());
                        plugin.getLogger().info("Using lang file \"" + fileName + "\" (Streamed from .jar)");
                    } catch (IOException e) {
                        plugin.getLogger().warning("Using default language values.");
                        plugin.debug("Using default language values (#3)");
                    }
                } else {
                    plugin.getLogger().warning("Using default language values.");
                    plugin.debug("Using default language values (#4, Reader is null)");
                }
            }
        }
        LangUtils.reload();
    }

    @Nonnull
    public String findTranslation(@Nonnull Message message) {
        String path = message.getPath();
        String finalMessage = getString(path);
        if (finalMessage != null)
            return finalMessage;
        // Value was missing
        String defaultMessage = message.getDefaultMessage();
        configFilePathValues.put(path, defaultMessage);
        if (file != null) {
            // Append missing entry to loaded language file
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write("\n# Scenario: " + message.getDescription());
                writer.write("\n# Available placeholders: " +
                        message.getAvailablePlaceholders().stream()
                        .map(Placeholder::toString).collect(Collectors.joining(", ")));
                writer.write("\n" + path + "=" + defaultMessage + "\n");
                plugin.getLogger().info("Missing translation for \"" + path + "\" has been added as \"" + defaultMessage + "\" to the selected language file.");
            } catch (IOException e) {
                plugin.debug("Failed to add language entry");
                plugin.debug(e);
                plugin.getLogger().severe("Failed to add missing translation for \"" + path + "\".");
            }
        }
        return defaultMessage;
    }

    @Override
    public String getString(@Nonnull String path) {
        for (Map.Entry<String, String> entry : configFilePathValues.entrySet())
            if (entry.getKey().equals(path))
                return entry.getValue();
        return null;
    }

    @Override
    public void load(@Nonnull File file) throws IOException {
        load(file.toPath());
    }

    public void load(Path path) throws IOException {
        this.file = path.toFile();
        loadFromStream(Files.lines(path));
    }

    @Override
    @Nonnull
    public String saveToString() {
        return String.join("\n", lines);
    }

    private void loadFromStream(@Nonnull Stream<String> lines) {
        lines
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .filter(l -> l.contains("="))
                .forEach(line -> {
                    String[] pair = line.split("=", 2);
                    String path = pair[0];
                    String value = pair[1];
                    configFilePathValues.put(path, value == null ? "" : value);
                });
    }

    @Override
    public void loadFromString(@Nonnull String s) {
        loadFromStream(Arrays.stream(s.split("\n")));
    }

    @Override
    @Nonnull
    protected String buildHeader() {
        return "";
    }

}
