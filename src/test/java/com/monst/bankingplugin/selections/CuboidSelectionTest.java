package com.monst.bankingplugin.selections;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

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
        assertEquals(300, sel.getVolume());
    }

    @Test
    void overlaps() {
        CuboidSelection sel = CuboidSelection.of(
                null, new Location(null, 1, 0, 1), new Location(null, 5, 9, 5));
        Polygonal2DSelection polySel = Polygonal2DSelectionTest.newSel(0, 2,
                5, 2,
                5, -6);
        assertTrue(sel.overlaps(polySel));
    }
}