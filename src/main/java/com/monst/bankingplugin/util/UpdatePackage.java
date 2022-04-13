package com.monst.bankingplugin.util;

import com.google.gson.JsonObject;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.*;

public class UpdatePackage implements Observable {

    private static final Path DOWNLOAD_PATH = Bukkit.getServer().getUpdateFolderFile().toPath().resolve("bankingplugin.incomplete");

    public enum State {
        /** The update has not begun downloading yet.*/
        INITIAL,

        /** The update is currently being downloaded.*/
        DOWNLOADING,

        /** The download process was paused.*/
        PAUSED,

        /** The download process has completed and the file is being validated.*/
        VALIDATING,

        /** The file has been successfully downloaded and validated.*/
        COMPLETED,

        /** The download failed and may be restarted.*/
        ERROR,

        /** The update is no longer the latest available version.*/
        OUTDATED
    }

    private final BankingPlugin plugin;
    private final String version;
    private final String remoteChecksum;
    private final String downloadLink;

    private State state = State.INITIAL;
    private URL fileUrl;
    private Long fileSize;
    private long bytesDownloaded = 0;
    private int downloadPercentage = 0;

    private final Set<GUI<?>> observers = new HashSet<>();

    public UpdatePackage(BankingPlugin plugin, JsonObject response) {
        this.plugin = plugin;
        this.version = response.get("name").getAsString();
        this.remoteChecksum = response.get("md5").getAsString();
        this.downloadLink = response.get("downloadUrl").getAsString();
    }

    private URL followRedirects(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setConnectTimeout(5000);
        con.connect();
        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP)
            return followRedirects(new URL(con.getHeaderField("Location")));
        return url;
    }

    /**
     * Downloads the update to the server's update folder under the same name as the current jar file.
     * If the download is successful, the update package is marked as {@link State#COMPLETED completed}.
     * If the download fails, the update package is placed in an {@link State#ERROR error} state and can be restarted.
     *
     * @param callback Called when the state changes.
     */
    public void download(Callback<State> callback) {
        if (!(state == State.INITIAL || state == State.PAUSED || state == State.ERROR))
            return; // Only continue if the package is in one of these states

        plugin.debug("Starting download of update package " + version + " (state = " + state + ")");
        boolean resume = state == State.PAUSED; // If the download was paused, continue from where it left off. Otherwise, start over.
        if (!resume) {
            bytesDownloaded = 0; // Reset the bytes downloaded counter
            downloadPercentage = 0; // Reset the download percentage
        }

        setState(State.DOWNLOADING);
        callback.onResult(state);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Create the update folder if it doesn't exist
            try {
                Files.createDirectories(DOWNLOAD_PATH.getParent());
            } catch (IOException e) {
                callback.callSyncError("Failed to create update folder.", e);
                setState(State.ERROR);
                return;
            }
            
            // Find the URL to download from
            if (fileUrl == null) {
                try {
                    fileUrl = followRedirects(new URL(downloadLink));
                } catch (MalformedURLException e) {
                    callback.callSyncError("Failed to parse download link: " + downloadLink, e);
                    setState(State.ERROR);
                    return;
                } catch (IOException e) {
                    callback.callSyncError("Failed to follow redirects to download url: " + downloadLink, e);
                    setState(State.ERROR);
                    return;
                }
            }
    
            // Establish a connection with the server
            URLConnection con;
            try {
                con = fileUrl.openConnection();
                if (bytesDownloaded > 0) // If the download is being resumed, only request the remaining bytes
                    con.setRequestProperty("Range", "bytes=" + bytesDownloaded + "-");
                con.setConnectTimeout(5000);
                con.connect();
            } catch (IOException e) {
                callback.callSyncError("Could not create connection to download URL: ", e);
                setState(State.ERROR);
                return;
            }

            if (fileSize == null) // If the file size is not known yet, get it from the server
                fileSize = con.getContentLengthLong();

            // Open an input stream from the connection and an output stream to the file
            try (InputStream urlIn = con.getInputStream();
                 // If the download is being resumed, append to what was already downloaded. Otherwise, overwrite.
                 OutputStream fileOut = Files.newOutputStream(DOWNLOAD_PATH, CREATE, resume ? APPEND : TRUNCATE_EXISTING)) {

                // Download the data 8KiB at a time
                byte[] buffer = new byte[8 * 1024];
                for (int bytesRead = urlIn.read(buffer); bytesRead != -1; bytesRead = urlIn.read(buffer)) {
                    fileOut.write(buffer, 0, bytesRead);
                    int newPercentage = (int) (100 * (bytesDownloaded += bytesRead) / fileSize);
                    if (newPercentage != downloadPercentage) {
                        this.downloadPercentage = newPercentage;
                        notifyObservers();
                        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                    }
                    // If the download has been paused or set outdated, stop the download
                    if (state == State.PAUSED || state == State.OUTDATED)
                        return;
                }
            } catch (IOException e) {
                callback.callSyncError("Could not download update.", e);
                setState(State.ERROR);
                return;
            }

            // Compare the downloaded file's checksum against the one on the server
            setState(State.VALIDATING);
            callback.callSyncResult(state);
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {} // Wait a second for better UX
            try {
                // Get an MD5 digest instance for calculating the checksum
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(Files.readAllBytes(DOWNLOAD_PATH));
                String localChecksum = toHexString(md5.digest());
                if (!localChecksum.equals(remoteChecksum)) {
                    callback.callSyncError("Checksum mismatch: " + localChecksum + " != " + remoteChecksum, new IllegalStateException());
                    setState(State.ERROR);
                    return;
                }
            } catch (NoSuchAlgorithmException e) { // Should not happen
                callback.callSyncError("MD5 algorithm not available.", e);
                setState(State.ERROR);
                return;
            } catch (IOException e) {
                callback.callSyncError("Could not read downloaded file to validate it.", e);
                setState(State.ERROR);
                return;
            }

            // Rename incomplete file to the plugin jar file name, replacing any existing file in the update folder with that name
            try {
                Files.move(DOWNLOAD_PATH, DOWNLOAD_PATH.resolveSibling(plugin.getJarFile().getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                callback.callSyncError("Could not rename update file.", e);
            }

            if (state == State.PAUSED || state == State.OUTDATED)
                return;
            setState(State.COMPLETED);
            callback.callSyncResult(state);
        });
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Pauses the download.
     */
    public void pauseDownload() {
        if (state == State.DOWNLOADING)
            setState(State.PAUSED);
        // Thread.join()
        // This should be a blocking operation until the download is paused
    }

    public void setOutdated() {
        setState(State.OUTDATED);
    }

    public int getDownloadPercentage() {
        return downloadPercentage;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
        notifyObservers();
    }

    public String getVersion() {
        return version;
    }

    public Set<GUI<?>> getObservers() {
        return observers;
    }

}
