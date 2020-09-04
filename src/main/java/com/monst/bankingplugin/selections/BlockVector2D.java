package com.monst.bankingplugin.selections;

import com.monst.bankingplugin.utils.Pair;

public class BlockVector2D extends Pair<Integer, Integer> {
    public BlockVector2D(int x, int z) {
        super(x, z);
    }
    public int getBlockX() { return super.getFirst(); }
    public int getBlockZ() { return super.getSecond(); }
}
