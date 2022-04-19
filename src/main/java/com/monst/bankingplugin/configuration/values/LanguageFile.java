package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import com.monst.pluginconfiguration.impl.PathConfigurationValue;
import com.monst.pluginconfiguration.validation.Bound;
import com.monst.pluginconfiguration.validation.PathValidation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageFile extends PathConfigurationValue {

    private final BankingPlugin plugin;
    private final Path langFolder;
    private final EnumMap<Message, String> translations;

    public LanguageFile(BankingPlugin plugin) {
        super(plugin, "language-file", Paths.get("en_US.lang"));
        this.plugin = plugin;
        this.langFolder = plugin.getDataFolder().toPath().resolve("lang");
        this.translations = new EnumMap<>(Message.class);
    }

    @Override
    protected ArgumentParseException createArgumentParseException(String input) {
        return new ArgumentParseException(Message.NOT_A_FILENAME.with(Placeholder.INPUT).as(input).translate(plugin));
    }

    @Override
    protected Bound<Path> getBound() {
        return PathValidation.isFile("lang");
    }

    @Override
    public void reload() {
        super.reload();
        loadTranslations();
    }

    @Override
    protected void afterSet() {
        loadTranslations();
    }

    public void loadTranslations() {
        Path defaultLangFile = langFolder.resolve(getDefaultValue());
        if (!Files.exists(defaultLangFile))
            plugin.saveResource("lang/en_US.lang", false);

        Path languageFile = langFolder.resolve(get());
        if (!Files.exists(languageFile)) {
            plugin.getLogger().warning("Could not find language file \"" + languageFile.getFileName() + "\".");
            plugin.debugf("Could not find language file \"%s\".", languageFile.getFileName());
            languageFile = defaultLangFile;
        }
    
        translations.clear();
    
        String fileName = languageFile.getFileName().toString();
        Map<String, String> fileTranslations;
        try (Stream<String> lines = Files.lines(languageFile)) {
            fileTranslations = lines
                    .filter(l -> !l.isEmpty())
                    .filter(l -> !l.startsWith("#"))
                    .filter(l -> l.contains("="))
                    .map(l -> l.split("=", 2))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1], (first, second) -> second));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load language file \"" + fileName + "\".");
            plugin.debug("Using default language values.");
            plugin.debug(e);
            return;
        }
    
        EnumSet<Message> missingMessages = EnumSet.allOf(Message.class);
        for (Iterator<Message> it = missingMessages.iterator(); it.hasNext(); ) {
            Message message = it.next();
            String translation = fileTranslations.remove(message.getPath());
            if (translation != null) {
                translations.put(message, ChatColor.translateAlternateColorCodes('&', translation));
                it.remove();
            }
        }
    
        if (!fileTranslations.isEmpty()) {
            plugin.getLogger().info("There are unused translations in language file \"" + fileName + "\".");
            plugin.getLogger().info("See debug log for details.");
            plugin.debugf("Unused translations found in language file \"%s\": %s", fileName, fileTranslations.keySet());
        }
    
        if (!missingMessages.isEmpty()) {
            try (BufferedWriter writer = Files.newBufferedWriter(languageFile, StandardOpenOption.APPEND)) {
                for (Message message : missingMessages) {
                    writer.write("\n# Example Scenario: " + message.getExampleScenario());
                    writer.write("\n# Available placeholders: " + message.getFormattedPlaceholdersList());
                    writer.write("\n" + message.getPath() + "=" + message.inEnglish()
                            .replaceAll("\u00A7", "&") + "\n");
                    plugin.getLogger().info("Missing translation for \"" + message.getPath() +
                            "\" has been added to the current language file.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to add missing translation(s) to the language file.");
                plugin.debug("Failed to add missing translation(s) to the language file.");
                plugin.debug(e);
            }
        }
    
        String locale = fileName.substring(0, fileName.length() - 5);
        plugin.getLogger().info("Using locale \"" + locale + "\"");
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length > 2)
            return Collections.emptyList();
        Stream.Builder<String> tabCompletions = Stream.builder();
        tabCompletions.accept(toString());
        tabCompletions.accept("en_US.lang");
        Path langFolder = plugin.getDataFolder().toPath().resolve("lang");
        try (Stream<Path> folderContents = Files.walk(langFolder)) {
            folderContents
                    .filter(Files::isRegularFile)
                    .map(langFolder::relativize)
                    .map(Path::toString)
                    .filter(fileName -> fileName.endsWith(".lang"))
                    .forEach(tabCompletions);
        } catch (IOException e) {
            plugin.debug(e);
        }
        return tabCompletions.build().distinct().collect(Collectors.toList());
    }

    public String getTranslation(Message message, String defaultTranslation) {
        return translations.getOrDefault(message, defaultTranslation);
    }

}
