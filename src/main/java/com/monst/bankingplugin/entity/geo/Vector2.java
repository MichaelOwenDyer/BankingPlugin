package com.monst.bankingplugin.entity.geo;

import jakarta.persistence.Embeddable;
import org.bukkit.block.Block;

import java.util.Objects;

@Embeddable
public class Vector2 {

    public Vector2() {}

    public Vector2(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Vector2(Block block) {
        this.x = block.getX();
        this.z = block.getZ();
    }

    int x;
    int z;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Vector3 toVector3(int y) {
        return new Vector3(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("[%d;%d]", x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Vector2 other = (Vector2) o;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

}
