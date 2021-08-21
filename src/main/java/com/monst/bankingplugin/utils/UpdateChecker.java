package com.monst.bankingplugin.utils;

import com.google.gson.*;
import com.monst.bankingplugin.BankingPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {

    private final BankingPlugin plugin;
    private String version = "";
    private String link = "";

    public UpdateChecker(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if an update is needed
     *
     * @return {@link Result#TRUE} if an update is available,
     *         {@link Result#FALSE} if no update is needed or
     *         {@link Result#ERROR} if an error occurred
     */
    public Result check() {
        try {
            plugin.debug("Checking for updates...");

			URL url = new URL("");
            URLConnection con = url.openConnection();
			con.setRequestProperty("", "BankingPlugin/UpdateChecker");

            InputStreamReader reader = new InputStreamReader(con.getInputStream());
            JsonElement element = new JsonParser().parse(reader);

            if (element.isJsonArray()) {
                JsonObject result = element.getAsJsonArray().get(0).getAsJsonObject();
                String id = result.get("id").getAsString();
                version = result.get("name").getAsString();
				link = id;
            } else {
                plugin.debug("Failed to check for updates");
                plugin.debug("Result: " + element);
                return Result.ERROR;
            }

            if (plugin.getDescription().getVersion().equals(version)) {
                plugin.debug("No update found");
                return Result.FALSE;
            } else {
                plugin.debug("Update found: " + version);
                return Result.TRUE;
            }

        } catch (JsonIOException | JsonSyntaxException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            plugin.debug("Failed to check for updates");
            plugin.debug(e);
            return Result.ERROR;
        }
        return null;
    }

    /**
     * @return Latest Version or <b>null</b> if no update is available
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Download Link of the latest version of <b>null</b> if no update is available
     */
    public String getLink() {
        return link;
    }

    public enum Result {
        TRUE,
        FALSE,
        ERROR
    }


}
