package com.monst.bankingplugin.entity.geo;

import jakarta.persistence.Embeddable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Objects;

@Embeddable
public class Vector3 {

    public static Vector3 min(Vector3 v1, Vector3 v2) {
        return new Vector3(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
    }

    public static Vector3 max(Vector3 v1, Vector3 v2) {
        return new Vector3(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
    }

    int x;
    int y;
    int z;

    public Vector3() {}

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public BlockFace getFace(Vector2 block) {
        if (this.x > block.x)
            return BlockFace.WEST;
        if (this.x < block.x)
            return BlockFace.EAST;
        if (this.z > block.z)
            return BlockFace.NORTH;
        if (this.z < block.z)
            return BlockFace.SOUTH;
        throw new IllegalArgumentException("Cannot get orientation to identical position");
    }

    public Block toBlock(World world) {
        return world.getBlockAt(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    @Override
    public String toString() {
        return String.format("[%d;%d;%d]", x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Vector3 other = (Vector3) o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
