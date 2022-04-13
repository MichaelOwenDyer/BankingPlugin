package com.monst.bankingplugin.persistence.service;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.gui.GUI;
import com.monst.bankingplugin.persistence.repository.EntityRepository;
import com.monst.bankingplugin.util.Callback;
import com.monst.bankingplugin.util.Observable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.RollbackException;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class EntityService<Entity, Repository extends EntityRepository<Entity>> implements Observable {

    final BankingPlugin plugin;
    final Supplier<EntityManager> emf;
    final Repository repo;
    final Set<GUI<?>> observers;

    public EntityService(BankingPlugin plugin, Supplier<EntityManager> emf, Repository repo) {
        this.plugin = plugin;
        this.emf = emf;
        this.repo = repo;
        this.observers = new HashSet<>();
    }

    public void save(Entity entity) {
        transaction(em -> em.persist(entity));
    }

    public void saveAll(Collection<Entity> entities) {
        transaction(em -> entities.forEach(em::persist));
    }

    public void update(Entity entity) {
        transaction(em -> em.merge(entity));
    }

    public void updateAll(Collection<Entity> entities) {
        transaction(em -> entities.forEach(em::merge));
    }

    public List<Entity> findAll() {
        return repo.findAll();
    }

    public void findAll(Callback<Collection<Entity>> callback) {
        async(repo::findAll, callback);
    }

    void transaction(Consumer<EntityManager> writeAction) {
        EntityManager writer = emf.get();
        EntityTransaction transaction = writer.getTransaction();
        try {
            transaction.begin();
            writeAction.accept(writer);
            transaction.commit();
        } catch (RollbackException e) {
            if (transaction.isActive())
                transaction.rollback();
        } finally {
            writer.close();
        }
    }

    <T> void async(Callable<T> callable, Callback<T> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                callback.callSyncResult(callable.call());
            } catch (Exception e) {
                callback.callSyncError("Error while running async database task.", e);
            }
        });
    }

    @Override
    public Set<GUI<?>> getObservers() {
        return observers;
    }

}
