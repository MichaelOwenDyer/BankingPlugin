package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Triple;
import org.bukkit.block.Block;

public class BlockVector3D extends Triple<Integer, Integer, Integer> implements Comparable<BlockVector3D> {
    public BlockVector3D(int x, int y, int z) {
        super(x, y, z);
    }
    public int getX() { return super.getFirst(); }
    public int getY() { return super.getSecond(); }
    public int getZ() { return super.getThird(); }

    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(getX(), getZ());
    }

    public BlockVector3D vectorTo(BlockVector3D to) {
        return new BlockVector3D(to.getX() - getX(), to.getY() - getY(), to.getZ() - getZ());
    }

    public static BlockVector3D fromBlock(Block block) {
        return new BlockVector3D(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public int compareTo(BlockVector3D other) {
        if (getX() != other.getX())
            return Integer.compare(getX(), other.getX());
        if (getY() != other.getY())
            return Integer.compare(getY(), other.getY());
        if (getZ() != other.getZ())
            return Integer.compare(getZ(), other.getZ());
        return 0;
    }

}
