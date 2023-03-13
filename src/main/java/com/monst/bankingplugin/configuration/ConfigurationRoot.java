package com.monst.bankingplugin.configuration;

import com.monst.bankingplugin.BankingPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public abstract class ConfigurationRoot extends ConfigurationBranch {
    
    private final BankingPlugin plugin;
    private final Path path;
    private final Yaml yaml;
    
    public ConfigurationRoot(BankingPlugin plugin, String fileName) {
        super(fileName.endsWith(".yml") ? fileName : fileName + ".yml");
        this.plugin = plugin;
        this.path = plugin.getDataFolder().toPath().resolve(getKey());
        this.yaml = createYaml();
    }
    
    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }
    
    private void copyDefaultFile() {
        InputStream in = plugin.getResource(path.getFileName().toString());
        if (in == null)
            return;
        try {
            Files.copy(in, path);
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to copy " + path.getFileName() + " to " + path.getParent() + "!", e);
        }
    }
    
    public void reload() {
        if (!Files.exists(path))
            copyDefaultFile();
        try (Reader in = Files.newBufferedReader(path)) {
            populate(yaml.load(in));
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to load " + path.getFileName() + "!", e);
            return;
        }
        save();
    }
    
    public void save() {
        try (Writer out = Files.newBufferedWriter(path)) {
            Files.createDirectories(path.getParent());
            yaml.dump(this.getAsYaml(), out);
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to save changes to " + path.getFileName() + "!", e);
        }
    }
    
}
