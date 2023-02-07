package com.monst.bankingplugin.entity;

import java.util.concurrent.ThreadLocalRandom;

public abstract class Entity {

    protected final int id;
    
    protected Entity() {
        this.id = ThreadLocalRandom.current().nextInt(90_000_000) + 10_000_000;
    }
    
    protected Entity(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return id == ((Entity) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
