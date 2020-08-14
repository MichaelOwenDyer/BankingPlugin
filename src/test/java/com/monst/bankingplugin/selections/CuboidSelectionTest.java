package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class CuboidSelectionTest {

    @Test
    void getMinimumPoint() {
        CuboidSelection sel = CuboidSelection.of(
                null, new Location(null, 0, 9, 5), new Location(null, 4, 0, 0));
        assertEquals(new Location(null, 0, 0, 0), sel.getMinimumPoint());
    }

    @Test
    void getMaximumPoint() {
        CuboidSelection sel = CuboidSelection.of(
                null, new Location(null, 0, 9, 5), new Location(null, 4, 0, 0));
        assertEquals(new Location(null, 4, 9, 5), sel.getMaximumPoint());
    }

    @Test
    void getCenterPoint() {
        CuboidSelection sel = CuboidSelection.of(
                null, new Location(null, 0, 9, 5), new Location(null, 4, 0, 0));
        assertEquals(new Location(null, 2, 4, 2), sel.getCenterPoint());
    }

    @Test
    void getVolume() {
        CuboidSelection sel = CuboidSelection.of(
                null, new Location(null, 0, 9, 5), new Location(null, 4, 0, 0));
        assertEquals(180, sel.getVolume());
    }

    @Test
    void contains() {
    }
}