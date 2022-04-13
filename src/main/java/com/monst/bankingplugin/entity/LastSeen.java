package com.monst.bankingplugin.entity;

import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.bukkit.OfflinePlayer;

import java.time.Instant;

@Entity
public class LastSeen {

    @Id
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer player;
    @Column(nullable = false)
    private Instant time;

    public LastSeen() {}

    public LastSeen(OfflinePlayer player) {
        this.player = player;
        this.time = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastSeen lastSeen = (LastSeen) o;
        return player.getUniqueId().equals(lastSeen.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return player.getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        return "LastSeen{" +
                "uuid=" + player.getUniqueId() +
                ", time=" + time +
                '}';
    }

}
