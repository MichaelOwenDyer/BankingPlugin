package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.Pair;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockVector2D extends Pair<Integer, Integer> {
    public BlockVector2D(int x, int z) {
        super(x, z);
    }
    public int getBlockX() { return super.getFirst(); }
    public int getBlockZ() { return super.getSecond(); }

    public BlockVector3D toBlockVector3D(int y) {
        return new BlockVector3D(getBlockX(), y, getBlockZ());
    }

    public Location toLocation(World world, int y) {
        return new Location(world, getBlockX(), y, getBlockZ());
    }

    public static BlockVector2D fromLocation(Location loc) {
        return new BlockVector2D(loc.getBlockX(), loc.getBlockZ());
    }
}
