package com.monst.bankingplugin.geo.locations;

import com.monst.bankingplugin.geo.BlockVector3D;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;

class SingleChestLocation extends ChestLocation {

    SingleChestLocation(World world, BlockVector3D v1) {
        super(world, v1);
    }

    @Override
    public Location getTeleportLocation() {
        return getMinimumLocation().add(0.5, 0.0, 0.5);
    }

    @Override
    @Nonnull
    public Iterator<Location> iterator() {
        return Collections.singletonList(v1.toLocation(world)).iterator();
    }

    @Override
    public byte getSize() {
        return 1;
    }

}
