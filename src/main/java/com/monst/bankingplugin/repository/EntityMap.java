package com.monst.bankingplugin.repository;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.gui.GUI;
import com.monst.bankingplugin.utils.Observable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class EntityMap<Location, Entity extends BankingEntity> implements Observable {

    private final BiMap<Location, Entity> biMap;
    private final Set<GUI<?>> observers;

    EntityMap() {
        this.biMap = HashBiMap.create();
        this.observers = new HashSet<>();
    }

    abstract Location getLocation(Entity entity);

    void put(Entity entity) {
        Entity a = biMap.forcePut(getLocation(entity), entity);
        if (a != null)
            notifyObservers();
    }

    void remove(Entity entity) {
        Entity a = biMap.remove(getLocation(entity));
        if (a != null)
            notifyObservers();
    }

    Set<Location> keySet() {
        return biMap.keySet();
    }

    Set<Entity> values() {
        return biMap.values();
    }

    Entity get(Location key) {
        return biMap.get(key);
    }

    Set<Map.Entry<Location, Entity>> entrySet() {
        return biMap.entrySet();
    }

    @Override
    public Set<GUI<?>> getObservers() {
        return observers;
    }

}
