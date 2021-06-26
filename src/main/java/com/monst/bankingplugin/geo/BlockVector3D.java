package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Triple;

public class BlockVector3D extends Triple<Integer, Integer, Integer> {
    public BlockVector3D(int x, int y, int z) {
        super(x, y, z);
    }
    public int getX() { return super.getFirst(); }
    public int getY() { return super.getSecond(); }
    public int getZ() { return super.getThird(); }
}
