package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageConfig extends FileConfiguration {

    private final BankingPlugin plugin;
    private final Path defaultLangFile;

    public LanguageConfig(BankingPlugin plugin) {
        this.plugin = plugin;
        this.defaultLangFile = plugin.getDataFolder().toPath().resolve("lang").resolve("en_US.lang");
        reload();
    }

    public void reload() {
        if (!Files.exists(defaultLangFile))
            plugin.saveResource("lang/en_US.lang", false);

        Path languageFile = defaultLangFile.resolveSibling(Config.languageFile.get());
        if (!Files.exists(languageFile)) {
            plugin.getLogger().warning("Could not find language file \"" + languageFile.getFileName() + "\".");
            plugin.debugf("Could not find language file \"%s\".", languageFile.getFileName());
            languageFile = defaultLangFile;
        }

        try {
            load(languageFile);
            plugin.getLogger().info("Using locale \"" + removeExtension(languageFile) + "\"");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load language file \"" + languageFile.getFileName() + "\".");
            plugin.debug("Using default language values.");
            plugin.debug(e);
        }
    }

    private void load(Path path) throws IOException {
        Map<String, String> fileTranslations = mapLines(Files.lines(path));
        EnumSet<Message> missingMessages = EnumSet.noneOf(Message.class);
        for (Message message : Message.values()) {
            String translation = fileTranslations.remove(message.getPath());
            if (translation == null) // Value was missing
                missingMessages.add(message);
            message.setTranslation(translation);
        }
        if (!fileTranslations.isEmpty()) {
            plugin.getLogger().info("There are unused translations in language file \"" + path.getFileName() + "\".");
            plugin.getLogger().info("See debug log for details.");
            plugin.debugf("Unused translations found in language file \"%s\": %s", path.getFileName(), fileTranslations.keySet());
        }
        if (missingMessages.isEmpty())
            return;

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            for (Message message : missingMessages) {
                writer.write("\n# Example Scenario: " + message.getExampleScenario());
                writer.write("\n# Available placeholders: " + message.getFormattedPlaceholdersList());
                writer.write("\n" + message.getPath() + "=" + message.getDefaultMessage() + "\n");
                plugin.getLogger().info("Missing translation for \"" + message.getPath() +
                        "\" has been added as \"" + message.getDefaultMessage() + "\" to the current language file.");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to add missing translation to the language file.");
            plugin.debug("Failed to add missing translation to the language file.");
            plugin.debug(e);
        }
    }

    private Map<String, String> mapLines(Stream<String> lines) {
        return lines
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .filter(l -> l.contains("="))
                .map(l -> l.split("=", 2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1], (existing, replacement) -> replacement));
    }

    @Override
    public void loadFromString(String s) {
        Map<String, String> map = mapLines(Arrays.stream(s.split("\n")));
        for (Message message : Message.values())
            message.setTranslation(map.get(message.getPath()));
    }

    @Override
    public String saveToString() {
        return Arrays.stream(Message.values())
                .map(message -> message.getPath() + "=" + message.getTranslation())
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected String buildHeader() {
        return "";
    }

    private static String removeExtension(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - 5);
    }

}
