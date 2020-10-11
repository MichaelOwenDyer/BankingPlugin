package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageConfig extends FileConfiguration {

    private final ArrayList<String> lines = new ArrayList<>();
    private final HashMap<String, String> values = new HashMap<>();

    private final BankingPlugin plugin;
    private final boolean showMessages;
    private File file;

    public LanguageConfig(BankingPlugin plugin, boolean showMessages) {
        this.plugin = plugin;
        this.showMessages = showMessages;
    }

    @Nonnull
    public String getString(@Nonnull Message message) {
        String path = message.getPath();
        String defaultMessage = message.getDefaultMessage();
        return Utils.nonNull(getString(path, null), () -> {
            // Value was missing
            values.put(path, defaultMessage);
            if (file != null) {
                // Append missing entry to loaded language file
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write("\n# Scenario: " + message.getDescription());
                    writer.write("\n# Available placeholders: "
                            + message.getAvailablePlaceholders().stream()
                                    .map(Placeholder::toString)
                                    .collect(Collectors.joining(", ")));
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
        });
    }

    @Override
    public String getString(@Nonnull String path, String defaultValue) {
        for (Map.Entry<String, String> entry : values.entrySet())
            if (entry.getKey().equals(path))
                return entry.getValue();
        return defaultValue;
    }

    @Override
    public void load(@Nonnull File file) throws IOException {
        this.file = file;

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder(32);

        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }

        fis.close();
        isr.close();
        br.close();

        loadFromString(sb.toString());
    }

    @Override
    @Nonnull
    public String saveToString() {
        return String.join("\n", lines);
    }

    @Override
    public void loadFromString(@Nonnull String s) {
        for (String line : s.split("\n")) {
            if (!line.isEmpty()) {
                this.lines.add(line);
                if (!line.startsWith("#") && line.contains("=")) {
                    String[] split = line.split("=");
                    if (split.length == 1)
                        values.put(split[0], "");
                    else if (split.length >= 2) {
                        String key = split[0];
                        String value = String.join("=", line.substring(line.indexOf('=') + 1).split("="));
                        values.put(key, value);
                    }
                }
            }
        }
    }

    @Override
    @Nullable
    protected String buildHeader() {
        return null;
    }
}