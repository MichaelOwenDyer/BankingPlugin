package com.monst.bankingplugin.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import java.util.Random;

@MappedSuperclass
public abstract class AbstractEntity {

    private static final Random idGenerator = new Random();
    protected void generateID() {
        this.id = idGenerator.nextInt(90_000_000) + 10_000_000;
    }

    @Id
    private int id;
    @Version
    private Integer version;

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return id == ((AbstractEntity) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
