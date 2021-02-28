package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Triple;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockVector3D extends Triple<Integer, Integer, Integer> {
    public BlockVector3D(int x, int y, int z) {
        super(x, y, z);
    }
    public int getX() { return super.getFirst(); }
    public int getY() { return super.getSecond(); }
    public int getZ() { return super.getThird(); }

    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(getX(), getZ());
    }

    public Location toLocation(World world) {
        return new Location(world, getX(), getY(), getZ());
    }

    public static BlockVector3D fromLocation(Location loc) {
        return new BlockVector3D(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
