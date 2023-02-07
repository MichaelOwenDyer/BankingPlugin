package com.monst.bankingplugin.persistence.repository;

import com.monst.bankingplugin.persistence.Query;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class LastSeenRepository {
    
    public void createTable(Connection con) throws SQLException {
        Query.of("CREATE TABLE IF NOT EXISTS LAST_SEEN("
                        + "player_uuid UUID NOT NULL PRIMARY KEY,"
                        + "timestamp TIMESTAMP NOT NULL)")
                .executeUpdate(con);
        
        Query.of("DROP TRIGGER IF EXISTS LAST_SEEN_CLEAR_UNUSED_ACCOUNT")
                .executeUpdate(con);
        Query.of("DROP TRIGGER IF EXISTS LAST_SEEN_CLEAR_UNUSED_BANK")
                .executeUpdate(con);
        Query.of("DROP PROCEDURE IF EXISTS clear_unused_last_seen")
                .executeUpdate(con);
        
        Query.of("CREATE PROCEDURE clear_unused_last_seen() MODIFIES SQL DATA BEGIN ATOMIC "
                        + "DELETE FROM LAST_SEEN "
                            + "WHERE player_uuid NOT IN (SELECT DISTINCT owner_uuid FROM BANK WHERE owner_uuid IS NOT NULL) "
                            + "AND player_uuid NOT IN (SELECT DISTINCT owner_uuid FROM ACCOUNT); "
                        + "END")
                .executeUpdate(con);
        Query.of("CREATE TRIGGER LAST_SEEN_CLEAR_UNUSED_ACCOUNT AFTER DELETE ON BANK CALL clear_unused_last_seen()")
                .executeUpdate(con);
        Query.of("CREATE TRIGGER LAST_SEEN_CLEAR_UNUSED_BANK AFTER DELETE ON BANK CALL clear_unused_last_seen()")
                .executeUpdate(con);
    }
    
    public Instant getLastSeenTime(Connection con, UUID uuid) throws SQLException {
        return Query.of("SELECT timestamp FROM LAST_SEEN WHERE player_uuid = ?")
                .with(uuid)
                .asOne(con, Instant.class);
    }
    
    public void updateLastSeenTime(Connection con, UUID uuid) throws SQLException {
        Query.of("DELETE FROM LAST_SEEN WHERE player_uuid = ?")
                .with(uuid)
                .executeUpdate(con);
        Query.of("INSERT INTO LAST_SEEN VALUES (?, ?)")
                .with(uuid, Timestamp.from(Instant.now()))
                .executeUpdate(con);
    }
    
    public void updateLastSeenTime(Connection con, Collection<OfflinePlayer> players) throws SQLException {
        Timestamp now = Timestamp.from(Instant.now());
        Query.of("DELETE FROM LAST_SEEN WHERE player_uuid IN (%s)")
                .in(players, OfflinePlayer::getUniqueId)
                .executeUpdate(con);
        Query.of("INSERT INTO LAST_SEEN VALUES (?, ?)")
                .batch(players)
                .with(player -> Arrays.asList(player.getUniqueId(), now))
                .executeUpdate(con);
    }
    
    public void deleteUnused(Connection con) throws SQLException {
        Query.of("CALL clear_unused_last_seen()").executeUpdate(con);
    }
    
}
