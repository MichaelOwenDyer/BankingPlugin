package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Pair;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockVector2D extends Pair<Integer, Integer> {
    public BlockVector2D(int x, int z) {
        super(x, z);
    }
    public int getX() { return super.getFirst(); }
    public int getZ() { return super.getSecond(); }

    public BlockVector3D toBlockVector3D(int y) {
        return new BlockVector3D(getX(), y, getZ());
    }

    public Location toLocation(World world, int y) {
        return new Location(world, getX(), y, getZ());
    }

    public static BlockVector2D fromLocation(Location loc) {
        return new BlockVector2D(loc.getBlockX(), loc.getBlockZ());
    }
}
