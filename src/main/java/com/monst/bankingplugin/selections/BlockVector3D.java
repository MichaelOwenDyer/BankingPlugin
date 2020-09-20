package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.Triple;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockVector3D extends Triple<Integer, Integer, Integer> {
    public BlockVector3D(int x, int y, int z) {
        super(x, y, z);
    }
    public int getBlockX() { return super.getFirst(); }
    public int getBlockY() { return super.getSecond(); }
    public int getBlockZ() { return super.getThird(); }

    public Location toLocation(World world) {
        return new Location(world, getBlockX(), getBlockY(), getBlockZ());
    }
}
