package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.LastSeen;
import com.monst.bankingplugin.persistence.repository.LastSeenRepository;
import com.monst.bankingplugin.util.Callback;
import jakarta.persistence.EntityManager;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LastSeenService extends EntityService<LastSeen, LastSeenRepository> {

    public LastSeenService(BankingPlugin plugin, Supplier<EntityManager> emf) {
        super(plugin, emf, new LastSeenRepository(emf));
    }

    public Instant getLastSeenTime(OfflinePlayer player) {
        return repo.getLastSeenTime(player);
    }

    public void updateLastSeen(OfflinePlayer player) {
        update(new LastSeen(player));
    }

    public void updateLastSeen(Set<OfflinePlayer> players) {
        updateAll(players.stream().map(LastSeen::new).collect(Collectors.toList()));
    }

    public void deleteUnused() {
        async(repo::findUnused, Callback.of(plugin, this::removeAll));
    }

    private void removeAll(Collection<OfflinePlayer> players) {
        transaction(em -> players.stream().map(player -> em.getReference(LastSeen.class, player.getUniqueId())).forEach(em::remove));
    }

}
