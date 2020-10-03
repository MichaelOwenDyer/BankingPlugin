package com.monst.bankingplugin.config;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    @Nonnull
    public String saveToString() {
        StringBuilder sb = new StringBuilder(lines.size() * 48);

        for (String line : lines)
            sb.append(line).append("\n");

        return sb.toString();
    }

    @Nullable
    public String getString(@Nonnull String path) {
        for (Map.Entry<String, String> entry : values.entrySet())
            if (entry.getKey().equals(path))
                return entry.getValue();
        return null;
    }

    @Override
    @Nonnull
    public String getString(@Nonnull String path, String defaultValue) {
        return Utils.nonNull(getString(path), () -> {
            // Value was missing
            values.put(path, defaultValue);
            if (file != null) {
                // Append missing entry to loaded language file
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(path + "=" + defaultValue + "\n");
                    if (showMessages)
                        plugin.getLogger().info("Missing translation for \"" + path + "\" has been added as \"" + defaultValue + "\" to the selected language file.");
                } catch (IOException e) {
                    plugin.debug("Failed to add language entry");
                    plugin.debug(e);
                    if (showMessages)
                        plugin.getLogger().severe("Failed to add missing translation for \"" + path + "\" to the selected langauge file.");
                }
            }
            return defaultValue;
        });
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
    public void loadFromString(@Nonnull String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                this.lines.add(line);

                if (!line.startsWith("#")) {
                    if (line.contains("=")) {
                        if (line.split("=").length >= 2) {
                            String key = line.split("=")[0];
                            StringBuilder sbValue = new StringBuilder();

                            for (int i = 1; i < line.split("=").length; i++) {
                                if (i > 1) {
                                    sbValue.append("=");
                                }
                                sbValue.append(line.split("=")[i]);
                            }

                            String value = sbValue.toString();

                            values.put(key, value);
                        } else if (line.split("=").length == 1) {
                            String key = line.split("=")[0];
                            values.put(key, "");
                        }
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