package com.monst.bankingplugin.selections;

import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolygonalSelectionTest {

    private static Constructor<PolygonalSelection> constructor;
    static {
        try {
            constructor = PolygonalSelection.class.getDeclaredConstructor(World.class, List.class, int.class, int.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
    }

    static PolygonalSelection newSel(int... args) {
        assertEquals(0, args.length % 2);
        List<BlockVector2D> points = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2) {
            points.add(new BlockVector2D(args[i], args[i + 1]));
        }
        try {
            return constructor.newInstance(null, points, 5, 5);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void getVolumeSquare() {
        long vol = newSel(
                0, 0,
                0, 10,
                10, 10,
                10, 0).getVolume();
        assertEquals(121, vol);
    }
    @Test
    public void getVolumeSquareReversed() {
        long vol = newSel(
                20, 10,
                20, 20,
                10, 20,
                10, 10).getVolume();
        assertEquals(121, vol);
    }
    @Test
    public void getVolumeZero() {
        long vol = newSel(
                0, 0,
                0, 0,
                0, 0).getVolume();
        assertEquals(1, vol);
    }
    @Test
    public void getVolumeOne() {
        long vol = newSel(
                1, 0,
                0, 1,
                -1, 0,
                0, -1).getVolume();
        assertEquals(5, vol);
    }
    @Test
    public void getVolumeDiagonal() {
        long vol = newSel(
                0, 4,
                4, 0,
                0, -4,
                -4, 0).getVolume();
        assertEquals(41, vol);
    }
    @Test
    public void getVolumeBigDiagonal() {
        long vol = newSel(
                0, 40,
                40, 0,
                0, -40,
                -40, 0).getVolume();
        assertEquals(3281, vol);
    }
    @Test
    public void getVolumeBigger() {
        long vol = newSel(
                0, -40,
                3, -30,
                13, -23,
                14, -14,
                40, 0,
                -17, 23).getVolume();
        assertEquals(1375, vol);
    }
}