package com.monst.bankingplugin.geo;

import com.monst.bankingplugin.utils.Pair;

public class Vector2D extends Pair<Integer, Integer> {
    public Vector2D(int x, int z) {
        super(x, z);
    }
    public int getX() { return super.getFirst(); }
    public int getZ() { return super.getSecond(); }

    public static Vector2D parse(String input) {
        String[] split = input.replaceAll("[\\[\\](){}]", "").split("\\s*[,.;:]\\s*");
        if (split.length < 2)
            return null;
        return new Vector2D(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}
