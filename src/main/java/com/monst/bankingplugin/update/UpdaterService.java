package com.monst.bankingplugin.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Promise;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class UpdaterService {
    
    private final BankingPlugin plugin;
    private Update latestUpdate;
    
    public UpdaterService(BankingPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks for an update on GitHub.
     * @return A promise which will resolve to the latest update if one is available.
     */
    public Promise<Update> checkForUpdate() {
        return Promise.async(plugin, () -> {
            latestUpdate = fetchUpdate();
            return latestUpdate;
        });
    }
    
    private Update fetchUpdate() throws IOException {
        plugin.debug("Checking for updates (current version: " + plugin.getDescription().getVersion() + ")...");
        URL github = new URL("https://api.github.com/repos/FreshLlamanade/BankingPlugin/releases");
        URLConnection con = github.openConnection();
        con.setConnectTimeout(5000);
        con.setRequestProperty("User-Agent", "BankingPlugin");
        con.setDoOutput(true);
    
        JsonElement response = new JsonParser().parse(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
    
        JsonArray releases = response.getAsJsonArray();
        if (releases.size() == 0) {
            // No releases available
            plugin.debug("No releases available.");
            return null;
        }
    
        plugin.debug("Found " + releases.size() + " releases: " + releases);
    
        String version = null;
        JsonObject jar = null;
        Iterator<JsonElement> releaseIterator = releases.iterator();
        while (jar == null) {
            if (!releaseIterator.hasNext()) {
                // No suitable release found
                plugin.debug("No suitable release found.");
                return null;
            }
            
            JsonObject release = releaseIterator.next().getAsJsonObject();
            version = release.get("name").getAsString();
            plugin.debug("Checking update version " + version);
    
            if (version.compareTo(plugin.getDescription().getVersion()) <= 0) {
                // This version is no newer than current version
                // No need to check further
                plugin.debug("Update version " + version + " is no newer than current version. Stopping search.");
                return latestUpdate;
            }
    
            if (plugin.config().ignoreUpdatesContaining.ignore(version)) { // This version is ignored
                plugin.debug("Skipping update version " + version + " because it contains an ignored tag.");
                continue;
            }
    
            jar = searchForJar(release.get("assets").getAsJsonArray());
        }
    
        Update update = latestUpdate;
        // This update is newer than any currently available update
        if (latestUpdate == null || latestUpdate.getVersion().compareTo(version) < 0) {
            plugin.debug("Creating update.");
        
            // Create a new update object
            URL url = new URL(jar.get("browser_download_url").getAsString());
            int downloadSize = jar.get("size").getAsInt();
            update = new Update(plugin, version, url, null, downloadSize);
        }
    
        if (plugin.config().downloadUpdatesAutomatically.get())
            update.download();
        return update;
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
        return latestUpdate;
    }
    
}
