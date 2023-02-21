package com.monst.bankingplugin.update;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.logging.Level;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

public class Download {
    
    private static class DownloadInterruptedException extends Exception {}
    
    private static final Path DOWNLOAD_PATH = Bukkit.getServer().getUpdateFolderFile().toPath().resolve("bankingplugin.download");
    
    private final BankingPlugin plugin;
    private final Update update;
    
    private boolean isRunning;
    private Duration duration = Duration.ZERO;
    private long bytesDownloaded;
    private int percentComplete;
    private Boolean validated; // Null if not yet attempted, true if validated, false if validation attempt failed (not necessarily invalid)
    private Exception exception;
    
    Download(BankingPlugin plugin, Update update) {
        this.plugin = plugin;
        this.update = update;
    }
    
    void start() {
        if (!isRunning && !isCompleted() && !failed()) {
            plugin.log(Level.INFO, "Downloading update " + update.getVersion() + "...");
            update.setState(Update.State.DOWNLOADING);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::run);
        }
    }
    
    void pause() {
        isRunning = false;
    }
    
    private void run() {
        long startTime = System.currentTimeMillis();
        isRunning = true;
        try {
            if (!Files.exists(DOWNLOAD_PATH)) // If the download file doesn't exist, be sure the directory does
                Files.createDirectories(DOWNLOAD_PATH.getParent());
            
            else if (Files.size(DOWNLOAD_PATH) != bytesDownloaded) {
                // If the incomplete file exists and its size is not equal to the number of bytes downloaded, delete it
                plugin.debug("Overwriting incomplete download file because its size was unexpected. Starting over...");
                bytesDownloaded = 0; // Don't need to delete the file, just reset the number of bytes downloaded
            }
            
            // Establish a connection with the server
            URLConnection con = requestBytesAtURL(update.getURL(), bytesDownloaded);
            streamBytesToDownloadFile(con);
            validate();
            
            // Rename incomplete file to the plugin jar file name, replacing any existing file in the update folder with that name
            Files.move(DOWNLOAD_PATH, DOWNLOAD_PATH.resolveSibling(plugin.getFileName()), REPLACE_EXISTING);
            
            duration = duration.plusMillis((System.currentTimeMillis() - startTime));
            update.setState(Update.State.SUCCESS);
            plugin.log(Level.INFO, "Download complete! Restart the server to apply the update.");
        } catch (DownloadInterruptedException ignored) {
            duration = duration.plusMillis(System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            exception = e;
            update.setState(Update.State.DOWNLOAD_FAILED);
            plugin.log(Level.SEVERE, "Download failed. Try again or update the plugin manually (version " + update.getVersion() + ").", e);
        }
        isRunning = false;
    }
    
    private static URLConnection requestBytesAtURL(URL url, long resumeFrom) throws IOException {
        URLConnection con = url.openConnection();
        con.setRequestProperty("Accept", "application/octet-stream");
        if (resumeFrom > 0) // If the download is being resumed, only request the remaining bytes
            con.setRequestProperty("Range", "bytes=" + resumeFrom + "-");
        con.setConnectTimeout(5000);
        con.connect();
        return con;
    }
    
    private void streamBytesToDownloadFile(URLConnection con) throws IOException, DownloadInterruptedException {
        long bytesPerPercentageStep = update.getFileSizeBytes() / 100;
        // Open an input stream from the connection and an output stream to the file
        try (InputStream urlIn = con.getInputStream();
             // If the download is being resumed, append to what was already downloaded. Otherwise, overwrite.
             OutputStream fileOut = Files.newOutputStream(DOWNLOAD_PATH, CREATE, bytesDownloaded > 0 ? APPEND : TRUNCATE_EXISTING)) {
            
            // Download the data 8KiB at a time
            byte[] buffer = new byte[8 * 1024];
            for (int bytesRead = urlIn.read(buffer); bytesRead != -1; bytesRead = urlIn.read(buffer)) {
                fileOut.write(buffer, 0, bytesRead);
                bytesDownloaded += bytesRead;
                if (bytesDownloaded >= bytesPerPercentageStep * (percentComplete + 1)) {
                    percentComplete = (int) (bytesDownloaded / bytesPerPercentageStep);
                    update.notifyObservers();
                    if (percentComplete % 14 == 0)
                        plugin.log(Level.INFO, "Downloaded " + percentComplete + "%");
                }
                // If the download has been paused or set outdated, stop the download
                if (!isRunning && bytesDownloaded < update.getFileSizeBytes()) {
                    update.setState(Update.State.PAUSED);
                    plugin.log(Level.INFO, "Download paused.");
                    throw new DownloadInterruptedException();
                }
            }
            plugin.log(Level.INFO, "Download complete.");
        }
    }
    
    private void validate() throws IOException {
        String remoteChecksum = update.getRemoteChecksum();
        if (remoteChecksum == null) {
            plugin.debug("No checksum provided by server. Skipping validation.");
            validated = false;
            return;
        }
        
        update.setState(Update.State.VALIDATING);
        plugin.log(Level.INFO, "Validating download...");
        try {
            Thread.sleep(1000); // Wait one second for better UX :)
        } catch (InterruptedException ignored) {}
        
        String checksum;
        try {
            // Get an MD5 digest instance for calculating the checksum
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(DOWNLOAD_PATH));
            checksum = toHexString(md5.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            // MD5 is guaranteed to be supported by all Java implementations, so NoSuchAlgorithmException should never be thrown
            // If we were still not able to validate the file for some reason, just log the error and return
            plugin.log(Level.WARNING, "Attempted to validate the download, but an error occurred. Skipping validation.", e);
            validated = false;
            return;
        }
        
        // Compare the downloaded file's checksum against the one on the server
        // If they don't match, throw an exception and force the user to download the file again
        if (!checksum.equals(remoteChecksum))
            throw new IOException("Downloaded file's checksum does not match the one on the server.");
        
        validated = true;
        plugin.log(Level.INFO, "Download validated.");
    }
    
    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
    
    public boolean isCompleted() {
        // Return true if file is fully downloaded and was at least attempted to be validated
        return bytesDownloaded == update.getFileSizeBytes() && validated != null;
    }
    
    public boolean failed() {
        return exception != null;
    }
    
    public int getPercentComplete() {
        return percentComplete;
    }
    
    public Duration getDuration() {
        return duration;
    }
    
    public boolean isValidated() {
        return validated != null && validated;
    }
    
}
