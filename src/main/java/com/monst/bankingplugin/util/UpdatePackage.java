package com.monst.bankingplugin.util;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;

import static java.nio.file.StandardOpenOption.*;

public class UpdatePackage {
    
    private static final Semaphore DOWNLOAD_PERMITTER = new Semaphore(1);

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

        /** The download failed and may be retried.*/
        ERROR,

        /** The update is no longer the latest available version.*/
        OUTDATED
    }

    private final BankingPlugin plugin;
    private final String version;
    private final URL fileURL;
    private final String remoteChecksum; // Nullable
    private final long filesize;

    private State state = State.INITIAL;
    private long bytesDownloaded;
    private int downloadPercentage;
    private boolean validated = false;

    public UpdatePackage(BankingPlugin plugin, String version, URL fileURL, String remoteChecksum, long filesize) {
        this.plugin = plugin;
        this.version = version;
        this.fileURL = fileURL;
        this.remoteChecksum = remoteChecksum;
        this.filesize = filesize;
    }

    /**
     * Downloads the update to the server's update folder under the same name as the current jar file.
     * If the download is successful, the update package is marked as {@link State#COMPLETED completed}.
     * If the download fails, the update package is placed in an {@link State#ERROR error} state and can be retried.
     *
     * @param callback Called when the state changes.
     */
    public void download(Callback<State> callback) {
        if (!DOWNLOAD_PERMITTER.tryAcquire()) {
            plugin.debug("Download already in progress, skipping download");
            return;
        }
        
        if (!(state == State.INITIAL || state == State.PAUSED || state == State.ERROR))
            return; // Only continue if the package is in one of these states

        plugin.debug("Starting download of update package " + version + " (state = " + state + ")");
        boolean resume = state == State.PAUSED; // If the download was paused, continue from where it left off. Otherwise, start over.
        if (!resume) {
            bytesDownloaded = 0; // Reset the bytes downloaded counter
            downloadPercentage = 0; // Reset the download percentage
        }
    
        this.state = State.DOWNLOADING;
        callback.onResult(state);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Create the update folder if it doesn't exist
            try {
                Files.createDirectories(DOWNLOAD_PATH.getParent());
            } catch (IOException e) {
                callback.callSyncError("Failed to create update folder.", e);
                this.state = State.ERROR;
                DOWNLOAD_PERMITTER.release();
                return;
            }
            
            // Establish a connection with the server
            URLConnection con;
            try {
                con = fileURL.openConnection();
                con.setRequestProperty("Accept", "application/octet-stream");
                if (bytesDownloaded > 0) // If the download is being resumed, only request the remaining bytes
                    con.setRequestProperty("Range", "bytes=" + bytesDownloaded + "-");
                con.setConnectTimeout(5000);
                con.connect();
            } catch (IOException e) {
                callback.callSyncError("Could not create connection to download URL: ", e);
                this.state = State.ERROR;
                DOWNLOAD_PERMITTER.release();
                return;
            }

            // Open an input stream from the connection and an output stream to the file
            try (InputStream urlIn = con.getInputStream();
                 // If the download is being resumed, append to what was already downloaded. Otherwise, overwrite.
                 OutputStream fileOut = Files.newOutputStream(DOWNLOAD_PATH, CREATE, resume ? APPEND : TRUNCATE_EXISTING)) {

                // Download the data 8KiB at a time
                byte[] buffer = new byte[8 * 1024];
                for (int bytesRead = urlIn.read(buffer); bytesRead != -1; bytesRead = urlIn.read(buffer)) {
                    fileOut.write(buffer, 0, bytesRead);
                    int newPercentage = (int) (100 * (bytesDownloaded += bytesRead) / filesize);
                    if (newPercentage != downloadPercentage) {
                        this.downloadPercentage = newPercentage;
                        callback.callSyncResult(State.DOWNLOADING);
                    }
                    // If the download has been paused or set outdated, stop the download
                    if (state == State.PAUSED || state == State.OUTDATED) {
                        callback.callSyncResult(state);
                        DOWNLOAD_PERMITTER.release();
                        return;
                    }
                }
            } catch (IOException e) {
                callback.callSyncError("Could not download update.", e);
                this.state = State.ERROR;
                DOWNLOAD_PERMITTER.release();
                return;
            }

            if (remoteChecksum != null) {
                // Compare the downloaded file's checksum against the one on the server
                this.state = State.VALIDATING;
                callback.callSyncResult(state);
                try {Thread.sleep(1000);} catch (InterruptedException ignored) {} // Wait one second for better UX :)
                try {
                    // Get an MD5 digest instance for calculating the checksum
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    md5.update(Files.readAllBytes(DOWNLOAD_PATH));
                    String localChecksum = toHexString(md5.digest());
                    if (!localChecksum.equals(remoteChecksum)) {
                        callback.callSyncError("Checksum mismatch!", new IllegalStateException(localChecksum + " != " + remoteChecksum));
                        this.state = State.ERROR;
                        DOWNLOAD_PERMITTER.release();
                        return;
                    }
                    validated = true;
                } catch (NoSuchAlgorithmException | IOException ignored) {
                    // Could not validate, everything is probably fine so just continue
                }
            }

            // Rename incomplete file to the plugin jar file name, replacing any existing file in the update folder with that name
            try {
                Files.move(DOWNLOAD_PATH, DOWNLOAD_PATH.resolveSibling(plugin.getJarFile().getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) { // Should never happen!
                callback.callSyncError("Could not rename update file.", e);
                this.state = State.ERROR;
                DOWNLOAD_PERMITTER.release();
                return;
            }
    
            this.state = State.COMPLETED;
            callback.callSyncResult(state);
            DOWNLOAD_PERMITTER.release();
        });
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Pauses the download.
     */
    public void pauseDownload() {
        if (state == State.DOWNLOADING)
            state = State.PAUSED;
    }

    public void setOutdated() {
        state = State.OUTDATED;
    }

    public State getState() {
        return state;
    }
    
    public String getVersion() {
        return version;
    }
    
    public int getDownloadPercentage() {
        return downloadPercentage;
    }
    
    public boolean isValidated() {
        return validated;
    }

}
