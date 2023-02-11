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
import java.util.function.Consumer;
import java.util.logging.Level;

import static java.nio.file.StandardOpenOption.*;

public class Update {
    
    private static final Path DOWNLOAD_PATH = Bukkit.getServer().getUpdateFolderFile().toPath().resolve("bankingplugin.incomplete");

    public enum State {
        /** The update has not begun downloading yet.*/
        INITIAL,

        /** The update is currently being downloaded.*/
        DOWNLOADING,

        /** The download process has completed and the file is being validated.*/
        VALIDATING,

        /** The file has been successfully downloaded and validated.*/
        COMPLETED,
    
        /** The download process was paused.*/
        PAUSED,

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

    private Download download;
    private State state = State.INITIAL;
    private long bytesDownloaded;
    
    public Update(BankingPlugin plugin, String version, URL fileURL, String remoteChecksum, long filesize) {
        this.plugin = plugin;
        this.version = version;
        this.fileURL = fileURL;
        this.remoteChecksum = remoteChecksum;
        this.filesize = filesize;
    }
    
    /**
     * Downloads the update to the server's update folder under the same name as the current jar file.
     * If the download is successful, the update is marked as {@link State#COMPLETED completed}.
     * If the download fails, the update is placed in an {@link State#ERROR error} state and can be retried.
     */
    public Download download() {
        if (download != null && download.isRunning()) {
            plugin.debug("Download already in progress, skipping call to download()");
            return download;
        }
        
        if (!(state == State.INITIAL || state == State.PAUSED || state == State.ERROR))
            return download; // Only continue if the update is in one of these states
    
        setState(State.DOWNLOADING);
        return download = new Download();
    }

    /**
     * Pauses the download.
     */
    public void pauseDownload() {
        if (state == State.DOWNLOADING)
            setState(State.PAUSED);
    }

    public void setOutdated() {
        setState(State.OUTDATED);
    }

    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
    }
    
    public String getVersion() {
        return version;
    }
    
    public boolean isCompleted() {
        return state == State.COMPLETED;
    }
    
    public class Download {
        
        private boolean isRunning;
        private Exception exception;
        private int downloadPercentage;
        private Consumer<Integer> onDownloadPercentageChange = percentage -> {};
        private Runnable onDownloadComplete = () -> {};
        private Consumer<Integer> onPause = percentage -> {};
        private Runnable onValidate = () -> {};
        private Consumer<Exception> onRejected = error -> {};
        
        public Download() {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::run);
        }
        
        private void run() {
            isRunning = true;
            try {
                // Create the update folder if it doesn't exist
                createDownloadDirectory();
                // Establish a connection with the server
                URLConnection con = connect();
                if (!downloadFile(con)) {
                    return;
                }
                setState(State.VALIDATING);
                validate();
                rename();
                setState(State.COMPLETED);
                plugin.log(Level.INFO, "Download complete! Restart the server to apply the update.");
            } catch (IOException e) {
                setState(State.ERROR);
                plugin.log(Level.SEVERE, "Download failed. Try again or update the plugin manually (version " + version + ").");
                plugin.debug(e);
                exception = e;
                if (onRejected != null)
                    onRejected.accept(e);
            }
            isRunning = false;
        }
        
        private void createDownloadDirectory() throws IOException {
            Files.createDirectories(DOWNLOAD_PATH.getParent());
        }
        
        private URLConnection connect() throws IOException {
            URLConnection con = fileURL.openConnection();
            con.setRequestProperty("Accept", "application/octet-stream");
            if (bytesDownloaded > 0) // If the download is being resumed, only request the remaining bytes
                con.setRequestProperty("Range", "bytes=" + bytesDownloaded + "-");
            con.setConnectTimeout(5000);
            con.connect();
            return con;
        }
        
        private boolean downloadFile(URLConnection con) throws IOException {
            plugin.log(Level.INFO, "Downloading BankingPlugin v" + version + "...");
            boolean resume = state == State.PAUSED; // If the download was paused, continue from where it left off. Otherwise, start over.
            if (!resume) {
                bytesDownloaded = 0; // Reset the bytes downloaded counter
                downloadPercentage = 0; // Reset the download percentage
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
                        downloadPercentage = newPercentage;
                        if (onDownloadPercentageChange != null)
                            onDownloadPercentageChange.accept(newPercentage);
                    }
                    // If the download has been paused or set outdated, stop the download
                    if (state == State.PAUSED || state == State.OUTDATED) {
                        if (onPause != null)
                            onPause.accept(downloadPercentage);
                        return false;
                    }
                }
                plugin.getLogger().info("Download complete.");
                if (onDownloadComplete != null)
                    onDownloadComplete.run();
                return true;
            }
        }
        
        private void validate() throws IOException {
            if (remoteChecksum == null)
                return;
            if (onValidate != null)
                onValidate.run();
            plugin.getLogger().info("Validating download...");
            try {
                Thread.sleep(1000); // Wait one second for better UX :)
            } catch (InterruptedException ignored) {}
            try {
                // Get an MD5 digest instance for calculating the checksum
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(Files.readAllBytes(DOWNLOAD_PATH));
                String downloadChecksum = toHexString(md5.digest());
                // Compare the downloaded file's checksum against the one on the server
                if (!downloadChecksum.equals(remoteChecksum))
                    throw new IOException("Checksums do not match! " + downloadChecksum + " != " + remoteChecksum);
            } catch (NoSuchAlgorithmException ignored) {
                // Not able to validate with MD5, everything is probably fine so just continue
            }
        }
        
        private String toHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        }
        
        private void rename() throws IOException {
            // Rename incomplete file to the plugin jar file name, replacing any existing file in the update folder with that name
            Files.move(DOWNLOAD_PATH, DOWNLOAD_PATH.resolveSibling(plugin.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        
        public Download onDownloadPercentageChange(Consumer<Integer> onDownloadPercentageChange) {
            this.onDownloadPercentageChange = onDownloadPercentageChange;
            return this;
        }
        
        public Download onPause(Consumer<Integer> onPause) {
            this.onPause = onPause;
            return this;
        }
        
        public Download onValidating(Runnable onValidate) {
            this.onValidate = onValidate;
            return this;
        }
        
        public Download onDownloadComplete(Runnable onDownloadComplete) {
            this.onDownloadComplete = onDownloadComplete;
            return this;
        }
        
        public Download catchError(Consumer<Exception> onRejected) {
            this.onRejected = onRejected;
            if (exception != null)
                onRejected.accept(exception);
            return this;
        }
        
        private boolean isRunning() {
            return isRunning;
        }
        
    }

}
