package com.monst.bankingplugin.entity.geo.region;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EasyMockExtension.class)
public class PolygonalBankRegionTest {
    
    @Mock
    private World worldMock;
    
    @TestSubject
    private BankRegion polygonalRegion = BankRegion.fromDatabase(worldMock, null, 100, null, null, 0, null, new int[]{0, 100, 100, 0}, new int[]{0, 0, 100, 100});
    private BankRegion polygonalRegion2 = BankRegion.fromDatabase(worldMock, null, 100, null, null, 0, null, new int[]{0, 100, 100, 0}, new int[]{0, -50, 50, 100});

    @Test
    void testCuboidMinMax() {
        assertEquals(0, polygonalRegion.getMinX());
        assertEquals(100, polygonalRegion.getMaxX());
        assertEquals(0, polygonalRegion.getMinY());
        assertEquals(100, polygonalRegion.getMaxY());
        assertEquals(0, polygonalRegion.getMinZ());
        assertEquals(100, polygonalRegion.getMaxZ());
    }
    
    @Test
    void testContains() {
        assertTrue(polygonalRegion.contains(0, 0, 0));
        assertTrue(polygonalRegion.contains(100, 0, 0));
        assertTrue(polygonalRegion.contains(100, 100, 0));
        assertTrue(polygonalRegion.contains(0, 100, 0));
        assertTrue(polygonalRegion.contains(0, 0, 100));
        assertTrue(polygonalRegion.contains(100, 0, 100));
        assertTrue(polygonalRegion.contains(100, 100, 100));
        assertTrue(polygonalRegion.contains(0, 100, 100));
        assertTrue(polygonalRegion.contains(50, 50, 50));
        assertFalse(polygonalRegion.contains(101, 0, 0));
        assertFalse(polygonalRegion.contains(0, 101, 0));
        assertFalse(polygonalRegion.contains(0, 0, 101));
        assertFalse(polygonalRegion.contains(-1, 0, 0));
        assertFalse(polygonalRegion.contains(0, -1, 0));
        assertFalse(polygonalRegion.contains(0, 0, -1));
    }
    
    @Test
    void testContains2() {
        assertTrue(polygonalRegion2.contains(0, 0, 0));
        assertTrue(polygonalRegion2.contains(100, 0, 0));
        assertTrue(polygonalRegion2.contains(100, 100, 0));
        assertTrue(polygonalRegion2.contains(0, 100, 0));
        assertTrue(polygonalRegion2.contains(0, 0, 100));
        assertTrue(polygonalRegion2.contains(100, 0, 100));
        assertTrue(polygonalRegion2.contains(100, 100, 100));
        assertTrue(polygonalRegion2.contains(0, 100, 100));
        assertTrue(polygonalRegion2.contains(50, 50, 50));
        assertFalse(polygonalRegion2.contains(101, 0, 0));
        assertFalse(polygonalRegion2.contains(0, 101, 0));
        assertFalse(polygonalRegion2.contains(0, 0, 101));
        assertFalse(polygonalRegion2.contains(-1, 0, 0));
        assertFalse(polygonalRegion2.contains(0, -1, 0));
        assertFalse(polygonalRegion2.contains(0, 0, -1));
    }
    
    @Test
    void testGetCenterBlock() {
        Block block = polygonalRegion.getCenterBlock();
        assertEquals(50, block.getX());
        assertEquals(50, block.getY());
        assertEquals(50, block.getZ());
    }

}
