package com.monst.bankingplugin.entity.geo.region;

import org.bukkit.World;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
public class BankRegionTest {
    
    @Mock
    private World worldMock;
    
    @TestSubject
    private BankRegion cuboidRegion = BankRegion.fromDatabase(worldMock, 0, 100, 0, 100, 0, 100, null, null);
    
    @TestSubject
    private BankRegion polygonalRegion = BankRegion.fromDatabase(worldMock, null, 100, null, null, 0, null, new int[]{0, 100, 100, 0}, new int[]{0, 0, 100, 100});

    @Test
    void testCuboidMinMax() {
        assertEquals(0, cuboidRegion.getMinX());
        assertEquals(100, cuboidRegion.getMaxX());
        assertEquals(0, cuboidRegion.getMinY());
        assertEquals(100, cuboidRegion.getMaxY());
        assertEquals(0, cuboidRegion.getMinZ());
        assertEquals(100, cuboidRegion.getMaxZ());
    }

}
