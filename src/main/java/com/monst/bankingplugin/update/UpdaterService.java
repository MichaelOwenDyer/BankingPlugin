package com.monst.bankingplugin.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Promise;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;

public class UpdaterService {
    
    private final BankingPlugin plugin;
    private Update update;
    
    public UpdaterService(BankingPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks for an update on GitHub.
     * @return A promise which will resolve to the latest update if one is available.
     */
    public Promise<Update> checkForUpdate() {
        plugin.log(Level.INFO, "Checking for updates...");
        return Promise.async(plugin, () -> {
            URL url = new URL("https://api.github.com/repos/FreshLlamanade/BankingPlugin/releases");
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestProperty("User-Agent", "BankingPlugin");
            con.setDoOutput(true);
    
            JsonElement response = new JsonParser().parse(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        
            JsonArray releases = response.getAsJsonArray();
            if (releases.size() == 0)
                // No releases available
                return update;
        
            String versionNumber = null;
            JsonObject jar = null;
            // Releases are sorted newest to oldest
            // Iterate backwards through releases to find the latest version
            for (int i = releases.size() - 1; i >= 0; i--) {
                JsonObject version = releases.get(i).getAsJsonObject();
                versionNumber = version.get("name").getAsString();
                plugin.debug("Checking update version " + versionNumber);
            
                if (versionNumber.compareTo(plugin.getDescription().getVersion()) <= 0) {
                    // This version is no newer than current version
                    // No need to check further
                    plugin.log(Level.INFO, "No updates found.");
                    return update;
                }
            
                if (plugin.config().ignoreUpdatesContaining.ignore(versionNumber)) { // This version is ignored
                    plugin.debug("Skipping update version " + versionNumber + " because it contains an ignored tag.");
                    continue;
                }
                
                jar = searchForJar(version.get("assets").getAsJsonArray());
                if (jar != null)
                    break;
            }
        
            if (jar == null) {
                plugin.log(Level.INFO, "No updates found.");
                return update;
            }
    
            plugin.log(Level.WARNING, "BankingPlugin version " + versionNumber + " is available!");
        
            // This update is newer than any currently available update
            if (update == null || update.getVersion().compareTo(versionNumber) < 0) {
                plugin.debug("Creating update.");
    
                // Create a new update object
                URL fileURL = new URL(jar.get("browser_download_url").getAsString());
                String checksum = Optional.ofNullable(jar.get("md5")).map(JsonElement::getAsString).orElse(null);
                int downloadSize = jar.get("size").getAsInt();
                update = new Update(plugin, versionNumber, fileURL, checksum, downloadSize);
            }
            
            if (plugin.config().downloadUpdatesAutomatically.get())
                update.download();
            return update;
        });
    }
    
    private JsonObject searchForJar(JsonArray assets) {
        for (JsonElement asset : assets) {
            JsonObject assetObject = asset.getAsJsonObject();
            if (assetObject.get("name").getAsString().endsWith(".jar"))
                return assetObject;
        }
        return null;
    }
    
    /**
     * Returns the latest update if one has already been found.
     * @return The latest update if one is available, or null if no update has been found yet.
     */
    public Update getUpdateIfAvailable() {
        return update;
    }
    
}
