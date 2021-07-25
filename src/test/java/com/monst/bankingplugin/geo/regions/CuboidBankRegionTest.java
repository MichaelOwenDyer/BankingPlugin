package com.monst.bankingplugin.geo.regions;

import com.monst.bankingplugin.geo.Vector3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuboidBankRegionTest {

    @Test
    void getMinimumPoint() {
        CuboidBankRegion region = CuboidBankRegion.of(
                null, new Vector3D(0, 9, 5), new Vector3D(4, 0, 0));
        assertEquals(new Vector3D(0, 0, 0), region.getMinimumBlock());
    }

    @Test
    void getMaximumPoint() {
        CuboidBankRegion region = CuboidBankRegion.of(
                null, new Vector3D(0, 9, 5), new Vector3D(4, 0, 0));
        assertEquals(new Vector3D(4, 9, 5), region.getMaximumBlock());
    }

    @Test
    void getCenterPoint() {
        CuboidBankRegion region = CuboidBankRegion.of(
                null, new Vector3D(0, 9, 5), new Vector3D(4, 0, 0));
        assertEquals(new Vector3D(2, 4, 2), region.getCenterPoint());
    }

    @Test
    void getVolume() {
        CuboidBankRegion region = CuboidBankRegion.of(
                null, new Vector3D(0, 9, 5), new Vector3D(4, 0, 0));
        assertEquals(300, region.getVolume());
    }

    @Test
    void overlaps() {
        CuboidBankRegion region = CuboidBankRegion.of(
                null, new Vector3D(1, 0, 1), new Vector3D(5, 9, 5));
        PolygonalBankRegion polyRegion = PolygonalBankRegionTest.newSel(0, 2,
                5, 2,
                5, -6);
        assertTrue(region.overlaps(polyRegion));
    }
}
