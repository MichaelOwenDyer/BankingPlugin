package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Pair;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockVector2D extends Pair<Integer, Integer> implements Comparable<BlockVector2D> {
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

    public BlockVector2D vectorTo(BlockVector2D to) {
        return new BlockVector2D(to.getX() - getX(), to.getZ() - getZ());
    }

    public static BlockVector2D fromLocation(Location loc) {
        return new BlockVector2D(loc.getBlockX(), loc.getBlockZ());
    }

    public boolean isAdjacent(BlockVector2D other) {
        int distanceX = Math.abs(getX() - other.getX());
        int distanceZ = Math.abs(getZ() - other.getZ());
        return !(distanceX > 1 || distanceZ > 1) && distanceX + distanceZ == 1;
    }

    @Override
    public int compareTo(BlockVector2D other) {
        if (getX() != other.getX())
            return Integer.compare(getX(), other.getX());
        if (getZ() != other.getZ())
            return Integer.compare(getZ(), other.getZ());
        return 0;
    }

}
