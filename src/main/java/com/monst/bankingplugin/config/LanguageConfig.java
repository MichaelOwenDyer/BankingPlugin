package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageConfig extends FileConfiguration {

    private final List<String> lines = new ArrayList<>();
    private final HashMap<String, String> configFilePathValues = new HashMap<>();

    private final BankingPlugin plugin;
    private final boolean showMessages;
    private File file;

    LanguageConfig(BankingPlugin plugin, boolean showMessages) {
        this.plugin = plugin;
        this.showMessages = showMessages;
    }

    @Nonnull
    public String findTranslation(@Nonnull Message message) {
        String path = message.getPath();
        String finalMessage = getString(path, null);
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
                if (showMessages)
                    plugin.getLogger().info("Missing translation for \"" + path + "\" has been added as \"" + defaultMessage + "\" to the selected language file.");
            } catch (IOException e) {
                plugin.debug("Failed to add language entry");
                plugin.debug(e);
                if (showMessages)
                    plugin.getLogger().severe("Failed to add missing translation for \"" + path + "\".");
            }
        }
        return defaultMessage;
    }

    @Override
    public String getString(@Nonnull String path, String defaultValue) {
        for (Map.Entry<String, String> entry : configFilePathValues.entrySet())
            if (entry.getKey().equals(path))
                return entry.getValue();
        return defaultValue;
    }

    @Override
    public void load(@Nonnull File file) throws IOException {
        this.file = file;
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            loadFromStream(br.lines());
        }
    }

    @Override
    @Nonnull
    public String saveToString() {
        return String.join("\n", lines);
    }

    public void loadFromStream(@Nonnull Stream<String> lines) {
        lines
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .filter(l -> l.contains("="))
                .forEach(line -> {
                    String[] split = line.split("=");
                    String key = split[0];
                    if (split.length == 1)
                        configFilePathValues.put(key, "");
                    else
                        configFilePathValues.put(key, line.substring(line.indexOf('=') + 1));
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
