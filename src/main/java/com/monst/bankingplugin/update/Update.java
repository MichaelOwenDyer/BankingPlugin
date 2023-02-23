package com.monst.bankingplugin.update;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.util.Observable;
import com.monst.bankingplugin.util.Observer;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Update implements Observable {
    
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
        SUCCESS,
        
        /** The download failed and may be retried.*/
        DOWNLOAD_FAILED,
        
        /** The downloaded file was corrupted and must be downloaded again.*/
        VALIDATION_FAILED
    }

    private final BankingPlugin plugin;
    private final String version;
    private final URL url;
    private final long fileSizeBytes;
    private String releaseNotes;
    private String checksum; // Nullable

    private State state = State.INITIAL;
    private Download download;
    
    public Update(BankingPlugin plugin, String version, URL url, String releaseNotes, String checksum, long fileSizeBytes) {
        this.plugin = plugin;
        this.version = version;
        this.url = url;
        this.releaseNotes = releaseNotes;
        this.checksum = checksum;
        this.fileSizeBytes = fileSizeBytes;
    }
    
    /**
     * Downloads the update to the server's update folder under the same name as the current jar file. If the download
     * is successful, the update is marked as {@link State#SUCCESS completed}. If the download fails, the update is
     * placed in an {@link State#DOWNLOAD_FAILED error} state and can be retried.
     */
    public void download() {
        if (download == null || download.failed())
            download = new Download(plugin, this);
        if (!download.isCompleted() && !download.isRunning())
            download.start();
    }
    
    public Download getDownload() {
        return download;
    }
    
    public void pauseDownload() {
        if (download != null)
            download.pause();
    }
    
    public boolean isDownloaded() {
        return download != null && download.isCompleted();
    }

    public State getState() {
        return state;
    }
    
    void setState(State state) {
        this.state = state;
        notifyObservers();
    }
    
    public String getVersion() {
        return version;
    }
    
    URL getURL() {
        return url;
    }
    
    public String getReleaseNotes() {
        return releaseNotes;
    }
    
    void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
    
    String getChecksum() {
        return checksum;
    }
    
    void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    boolean isOlderThan(String version) {
        return this.version.compareTo(version) < 0;
    }
    
    boolean isSameVersion(String version) {
        return this.version.equals(version);
    }
    
    private final Set<Observer> observers = new HashSet<>();
    @Override
    public Set<Observer> getObservers() {
        return observers;
    }

}
