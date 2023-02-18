package com.monst.bankingplugin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monst.bankingplugin.BankingPlugin;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UpdaterService {
    
    private final BankingPlugin plugin;
    private Update update;
    
    public UpdaterService(BankingPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if an update is needed.
     */
    public Promise<Update> checkForUpdate() {
        plugin.debug("Checking for updates...");
        return Promise.async(plugin, () -> {
            JsonElement response;
            URL url = new URL("https://api.github.com/repos/FreshLlamanade/BankingPlugin/releases");
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestProperty("User-Agent", "BankingPlugin");
            con.setDoOutput(true);
        
            response = new JsonParser().parse(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        
            // Releases are sorted by date, newest first
            JsonArray releases = response.getAsJsonArray();
        
            if (releases.size() == 0)
                // No versions available
                return update;
        
            String versionNumber = null;
            JsonObject jar = null;
            for (int i = releases.size() - 1; i >= 0; i--) {
                JsonObject version = releases.get(i).getAsJsonObject();
                versionNumber = version.get("name").getAsString();
                plugin.debug("Checking version " + versionNumber);
            
                if (versionNumber.compareTo(plugin.getDescription().getVersion()) <= 0) {
                    // This version is no newer than current version
                    // No need to check further
                    return update;
                }
            
                if (plugin.config().ignoreUpdatesContaining.ignore(versionNumber)) { // This version is ignored
                    plugin.debug("Skipping version " + versionNumber + " because it contains an ignored tag.");
                    continue;
                }
            
                for (JsonElement asset : version.get("assets").getAsJsonArray()) {
                    if (((JsonObject) asset).get("name").getAsString().endsWith(".jar")) {
                        // Found the latest non-ignored jar available, and it is newer than the current version
                        jar = (JsonObject) asset;
                        break;
                    }
                }
            }
        
            if (jar == null)
                // No suitable update found
                return update;
    
            plugin.debug("Found latest version: " + versionNumber);
        
            // An update already exists newer or equal to this one
            if (update != null && update.getVersion().compareTo(versionNumber) > 0)
                return update;
        
            if (update != null)
                update.setOutdated();
        
            // Create a new update package
            plugin.debug("Creating new update package.");
            URL fileURL;
            fileURL = new URL(jar.get("browser_download_url").getAsString());
            String checksum = Optional.ofNullable(jar.get("md5")).map(JsonElement::getAsString).orElse(null);
            int downloadSize = jar.get("size").getAsInt();
        
            update = new Update(plugin, versionNumber, fileURL, checksum, downloadSize);
            return update;
        });
    }
    
    public Update getUpdateIfAvailable() {
        return update;
    }
    
}
